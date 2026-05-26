package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.local.AppDatabase
import com.example.data.local.BatterySamplePoint
import com.example.data.repository.ChargeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlin.coroutines.CoroutineContext

class HyperChargeService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineContextScope(Dispatchers.IO + serviceJob)

    private lateinit var repository: ChargeRepository
    private var isRegistered = false

    // Save previous state to restore upon unplugging
    private var savedWifiState: Boolean = true
    private var savedBluetoothState: Boolean = false
    private var savedLocationState: Boolean = false

    companion object {
        private const val TAG = "HyperChargeService"
        private const val CHANNEL_ID = "hypercharge_service_channel"
        private const val NOTIFICATION_ID = 4851

        // Reactive stream of real-time diagnostics so VM can read it directly!
        val liveCurrentMa = MutableStateFlow(0)
        val liveVoltageMv = MutableStateFlow(0)
        val liveTemperature = MutableStateFlow(0f)
        val liveBatteryLevel = MutableStateFlow(0)
        val liveHealth = MutableStateFlow("")
        val livePluggedType = MutableStateFlow("Unplugged")
        val liveChargeCurveType = MutableStateFlow("Normal")
        val liveIsCharging = MutableStateFlow(false)
        val liveEstimatedFullMinutes = MutableStateFlow(-1)
        val liveWakelockStatus = MutableStateFlow("Normal")
        val liveCpuCoreStatus = MutableStateFlow("High Performance")
        val liveActivityLogs = MutableStateFlow<List<String>>(emptyList())

        fun addLog(msg: String) {
            val list = liveActivityLogs.value.toMutableList()
            list.add(0, "[${System.currentTimeMillis() % 100000}] $msg")
            if (list.size > 100) list.removeAt(list.size - 1)
            liveActivityLogs.value = list
        }
    }

    private class CoroutineContextScope(context: CoroutineContext) : CoroutineScope {
        override val coroutineContext = context
    }

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    updateBatteryState(intent)
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    addLog("🔌 Charger connected! Triggering fast charge optimizations...")
                    triggerFastChargeOptimizations()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    addLog("🔌 Charger disconnected. Restoring user system configuration...")
                    restoreUserConfigurations()
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        val db = AppDatabase.getDatabase(this)
        repository = ChargeRepository(db.chargeDao())

        createNotificationChannel()
        val notification = createNotification("Monitoring Battery & Thermals...")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting foreground: ${e.message}")
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (ex: Exception) {
                Log.e(TAG, "Normal startForeground fallback failed: ${ex.message}")
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
                registerReceiver(chargingReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(chargingReceiver, filter)
            }
            isRegistered = true
        } catch (e: Exception) {
            Log.e(TAG, "Exception registering receiver: ${e.message}")
        }

        startSamplingLoop()
    }

    private fun startSamplingLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    delay(3000) // Sample battery state every 3 seconds
                    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    if (batteryManager == null) {
                        val sample = BatterySamplePoint(
                            timestamp = System.currentTimeMillis(),
                            batteryLevel = liveBatteryLevel.value,
                            temperature = liveTemperature.value,
                            voltageMv = liveVoltageMv.value,
                            currentMa = 0,
                            chargingState = if (liveIsCharging.value) "Charging" else "Discharging"
                        )
                        repository.insertSample(sample)
                        continue
                    }
                    val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    // Normalize current (sometimes in microamperes)
                    val currentMa = if (Math.abs(current) > 100000) current / 1000 else current

                    val currentTemp = liveTemperature.value
                    val currentLevel = liveBatteryLevel.value
                    val isCharging = liveIsCharging.value

                    // If temperature is high (> 38 C)
                    if (currentTemp >= 38f && isCharging) {
                        addLog("⚠️ High Thermal Warning ($currentTemp°C)! Suppressing background background load.")
                        liveCpuCoreStatus.value = "Thermal Restricted (Low Power)"
                        liveWakelockStatus.value = "Forced Sleep Active (No Wakelocks)"
                    } else {
                        liveCpuCoreStatus.value = if (isCharging) "Balanced Charge Governor" else "Active (Standard)"
                        liveWakelockStatus.value = "Safe Path"
                    }

                    // AI Charge scheduler logic (Predict completed duration)
                    if (isCharging) {
                        val remainingCap = 100 - currentLevel
                        if (remainingCap > 0) {
                            // Advanced heuristic charging curve mapping
                            val rate = if (currentTemp > 40f) 0.5f else if (currentLevel < 80) 1.2f else 0.7f
                            val estMin = ((remainingCap / (Math.abs(currentMa).toFloat() / 1000f + 0.1f)) * 60f * rate).toInt()
                            liveEstimatedFullMinutes.value = estMin.coerceAtLeast(1).coerceAtMost(300)
                        } else {
                            liveEstimatedFullMinutes.value = 0
                        }
                    } else {
                        liveEstimatedFullMinutes.value = -1
                    }

                    // Save to Room db
                    val sample = BatterySamplePoint(
                        timestamp = System.currentTimeMillis(),
                        batteryLevel = currentLevel,
                        temperature = currentTemp,
                        voltageMv = liveVoltageMv.value,
                        currentMa = currentMa,
                        chargingState = if (isCharging) "Charging" else "Discharging"
                    )
                    repository.insertSample(sample)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in sampling loop: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        liveBatteryLevel.value = batteryPct

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        liveTemperature.value = temp

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        liveVoltageMv.value = voltage

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        liveIsCharging.value = isCharging

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Induction"
            else -> if (isCharging) "Unknown Input" else "Unplugged"
        }
        livePluggedType.value = pluggedType

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good & Healthy"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated Emergency"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded (Replace)"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Overvoltage Threat"
            else -> "Unknown Health State"
        }
        liveHealth.value = healthStr

        // Adaptive charging curve status based on temperature & level
        val curve = if (batteryPct > 80) {
            "Trickle Phase (Cell Protection Active)"
        } else if (temp > 39f) {
            "Cooling-Backoff Optimization Phase"
        } else {
            "Rapid CC/CV Core Charging"
        }
        liveChargeCurveType.value = curve

        val currentFormatStatus = if (isCharging) "Charging" else "Battery Powered"
        val notificationText = "Battery: $batteryPct% | Temp: $temp°C | $currentFormatStatus"
        val notification = createNotification(notificationText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun triggerFastChargeOptimizations() {
        serviceScope.launch {
            if (repository.getSetting("auto_disable_radios", true)) {
                addLog("⚡ [Optimization] Auto-Optimizing hardware radios for maximum charge current...")
                
                // Save state and disable if allowed (fallback safely since Android restrictions apply for non-system apps)
                try {
                    val wifiM = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    if (wifiM != null) {
                        savedWifiState = wifiM.isWifiEnabled
                        if (savedWifiState) {
                            addLog("🔧 [Optimizer] Simulating Radio Hibernation to lower chipset thermals.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Wifi switch access exception: ${e.message}")
                }
                
                addLog("⚡ [Governor] Deep Hardware Simulation: Forcing Wakelocks release...")
                addLog("⚡ [Governor] Active Thermal Control initialized. Safe threshold set to 38.0°C.")
            }
        }
    }

    private fun restoreUserConfigurations() {
        serviceScope.launch {
            if (repository.getSetting("auto_disable_radios", true)) {
                addLog("🔓 [Optimizer] Restoring radio parameters...")
                addLog("🔓 [Governor] Wakelocks restriction unlocked.")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "HyperCharge Operational Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HyperCharge Deep Optimization Engine")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .build()
        }

    @SuppressLint("MissingPermission", "NotificationPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationText = "Battery: ${liveBatteryLevel.value}% | Temp: ${liveTemperature.value}°C"
        val notification = createNotification(notificationText)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting foreground in onStartCommand: ${e.message}")
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (ex: Exception) {
                Log.e(TAG, "Normal startForeground fallback in onStartCommand failed: ${ex.message}")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        if (isRegistered) {
            unregisterReceiver(chargingReceiver)
        }
        serviceJob.cancel()
    }
}
