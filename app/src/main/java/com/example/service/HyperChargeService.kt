package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HyperChargeService : Service() {

    companion object {
        // Reactive streams of real-time diagnostics
        val liveCurrentMa = MutableStateFlow(0)
        val liveVoltageMv = MutableStateFlow(0)
        val liveTemperature = MutableStateFlow(0f)
        val liveBatteryLevel = MutableStateFlow(0)
        val liveHealth = MutableStateFlow("Good")
        val livePluggedType = MutableStateFlow("Unplugged")
        val liveChargeCurveType = MutableStateFlow("Normal")
        val liveIsCharging = MutableStateFlow(false)
        val liveEstimatedFullMinutes = MutableStateFlow(-1)
        val liveWakelockStatus = MutableStateFlow("Normal")
        val liveCpuCoreStatus = MutableStateFlow("High Performance")
        val liveActivityLogs = MutableStateFlow<List<String>>(emptyList())
        val isAutoRepairEnabled = MutableStateFlow(true)

        fun addLog(msg: String) {
            val list = liveActivityLogs.value.toMutableList()
            list.add(0, "[${System.currentTimeMillis() % 100000}] $msg")
            if (list.size > 100) list.removeAt(list.size - 1)
            liveActivityLogs.value = list
        }
    }

    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isReceiverRegistered = false

    private val chargingReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    updateBatteryState(intent)
                }
            }
        }
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0
        liveBatteryLevel.value = batteryPct

        val temp = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        liveTemperature.value = temp

        val voltage = intent.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0)
        liveVoltageMv.value = voltage

        val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                status == android.os.BatteryManager.BATTERY_STATUS_FULL
        liveIsCharging.value = isCharging
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        try {
            val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else {
                0
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(chargingReceiver, filter, receiverFlags)
            } else {
                registerReceiver(chargingReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        startAutoRepairMonitor()
    }

    private fun startForegroundNotification() {
        val channelId = "hypercharge_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "HyperCharge Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs background battery optimization"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("HyperCharge Engine Active")
            .setContentText("Monitoring battery state and performing auto repairs")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
            
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startAutoRepairMonitor() {
        serviceJob = scope.launch {
            while (true) {
                try {
                    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
                    val currentMa = if (batteryManager != null) {
                        val current = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                        if (Math.abs(current) > 100000) current / 1000 else current
                    } else {
                        0
                    }
                    liveCurrentMa.value = currentMa

                    if (isAutoRepairEnabled.value && liveIsCharging.value) {
                        val temp = liveTemperature.value
                        if (temp > 38.0f) {
                            performAutoRepair("High Temperature ($temp C)")
                        } else if (currentMa < -1000) { // High drain
                            performAutoRepair("High Power Draw")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(60000) // check every 60 seconds
            }
        }
    }
    
    private fun performAutoRepair(reason: String) {
        addLog("🛡️ [Auto-Repair] Triggered due to: $reason")
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val pm = packageManager
            val packages = pm.getInstalledPackages(0)
            var killedCount = 0
            for (packageInfo in packages) {
                val appInfo = packageInfo.applicationInfo
                if (appInfo != null && packageInfo.packageName != packageName && 
                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                    am.killBackgroundProcesses(packageInfo.packageName)
                    killedCount++
                }
            }
            addLog("🔧 [Auto-Repair] Suppressed $killedCount background processes.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(chargingReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
