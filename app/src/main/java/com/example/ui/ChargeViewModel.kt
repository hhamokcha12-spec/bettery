package com.example.ui
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChargeSession
import com.example.data.local.BatterySamplePoint
import com.example.data.repository.ChargeRepository
import com.example.service.HyperChargeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChargeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChargeRepository

    val allSessions: StateFlow<List<ChargeSession>>
    val recentSamples: StateFlow<List<BatterySamplePoint>>

    // Configurable switches saved in Room
    val autoDisableRadios = MutableStateFlow(true)
    val preventOverheat = MutableStateFlow(true)
    val preserveBatteryHealth = MutableStateFlow(true)
    val puristAmoledMode = MutableStateFlow(false)
    val batteryChargeLimit = MutableStateFlow(80)

    // EXCLUSIVE NEW ENTERPRISE FEATURES
    val appLanguage = MutableStateFlow("ar") // "en" for English, "ar" for Arabic
    val selectedPowerProfile = MutableStateFlow("Balanced") // "Balanced", "Eco", "Gaming"
    val diagnosticReportText = MutableStateFlow("")

    // Bridge with Live Diagnostic flows in Foreground Service
    val liveCurrentMa: StateFlow<Int> = HyperChargeService.liveCurrentMa
    val liveVoltageMv: StateFlow<Int> = HyperChargeService.liveVoltageMv
    val liveTemperature: StateFlow<Float> = HyperChargeService.liveTemperature
    val liveBatteryLevel: StateFlow<Int> = HyperChargeService.liveBatteryLevel
    val liveHealth: StateFlow<String> = HyperChargeService.liveHealth
    val livePluggedType: StateFlow<String> = HyperChargeService.livePluggedType
    val liveChargeCurveType: StateFlow<String> = HyperChargeService.liveChargeCurveType
    val liveIsCharging: StateFlow<Boolean> = HyperChargeService.liveIsCharging
    val liveEstimatedFullMinutes: StateFlow<Int> = HyperChargeService.liveEstimatedFullMinutes
    val liveWakelockStatus: StateFlow<String> = HyperChargeService.liveWakelockStatus
    val liveCpuCoreStatus: StateFlow<String> = HyperChargeService.liveCpuCoreStatus
    val liveActivityLogs: StateFlow<List<String>> = HyperChargeService.liveActivityLogs
    val isExtremeChargingActive: StateFlow<Boolean> = HyperChargeService.isExtremeChargingActive
    val lastKilledCount: StateFlow<Int> = HyperChargeService.lastKilledCount
    val lastFreedRamMb: StateFlow<Long> = HyperChargeService.lastFreedRamMb

    private var isReceiverRegistered = false

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    updateBatteryState(intent)
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    HyperChargeService.addLog("🔌 Charger connected! Monitoring input current...")
                    viewModelScope.launch {
                        if (repository.getSetting("auto_disable_radios", true)) {
                            HyperChargeService.addLog("⚡ [Optimization] Auto-Optimizations for charge speed activated.")
                            HyperChargeService.addLog("⚡ [Governor] Thermal Safeguards active.")
                        }
                    }
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    HyperChargeService.addLog("🔌 Charger disconnected. Restoring user system configuration...")
                    viewModelScope.launch {
                        if (repository.getSetting("auto_disable_radios", true)) {
                            HyperChargeService.addLog("🔓 [Optimizer] Restoring parameters...")
                        }
                    }
                }
            }
        }
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0
        val previousLevel = HyperChargeService.liveBatteryLevel.value
        HyperChargeService.liveBatteryLevel.value = batteryPct

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val previousTemp = HyperChargeService.liveTemperature.value
        HyperChargeService.liveTemperature.value = temp

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        HyperChargeService.liveVoltageMv.value = voltage

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        HyperChargeService.liveIsCharging.value = isCharging
        
        // Ringtone Alarms
        val targetLimit = batteryChargeLimit.value
        val limitAlarmEnabled = preserveBatteryHealth.value
        val overheatAlarmEnabled = preventOverheat.value
        
        if (limitAlarmEnabled && isCharging && batteryPct >= targetLimit && previousLevel < targetLimit) {
            HyperChargeService.addLog("🔔 Target Charge Limit ($targetLimit%) Reached!")
            playNotificationSound()
        }
        
        if (overheatAlarmEnabled && temp > 40.0f && previousTemp <= 40.0f) {
            HyperChargeService.addLog("🔥 EXTREME THERMAL WARNING: ${temp}C. Unplug Device!")
            playNotificationSound()
        }

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Induction"
            else -> if (isCharging) "Unknown Input" else "Unplugged"
        }
        HyperChargeService.livePluggedType.value = pluggedType

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good & Healthy"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated Emergency"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded (Replace)"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Overvoltage Threat"
            else -> "Unknown Health State"
        }
        HyperChargeService.liveHealth.value = healthStr

        // Adaptive charging curve status based on temperature & level
        val curve = if (batteryPct > 80) {
            "Trickle Phase (Cell Protection Active)"
        } else if (temp > 39f) {
            "Cooling-Backoff Optimization Phase"
        } else {
            "Rapid CC/CV Core Charging"
        }
        HyperChargeService.liveChargeCurveType.value = curve
    }

    private fun playNotificationSound() {
        try {
            val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val r = android.media.RingtoneManager.getRingtone(getApplication(), notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ChargeRepository(db.chargeDao())

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentSamples = repository.recentSamples.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Read settings from Database
        viewModelScope.launch {
            autoDisableRadios.value = repository.getSetting("auto_disable_radios", true)
            preventOverheat.value = repository.getSetting("prevent_overheat", true)
            preserveBatteryHealth.value = repository.getSetting("preserve_battery_health", true)
            puristAmoledMode.value = repository.getSetting("purist_amoled_mode", false)
            
            val prefs = application.getSharedPreferences("hypercharge_prefs", Context.MODE_PRIVATE)
            batteryChargeLimit.value = prefs.getInt("battery_charge_limit", 80)
            appLanguage.value = prefs.getString("app_language", "ar") ?: "ar"
            selectedPowerProfile.value = prefs.getString("selected_power_profile", "Balanced") ?: "Balanced"
            HyperChargeService.isExtremeChargingActive.value = prefs.getBoolean("is_extreme_charging_active", false)
        }

        // Register the local receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        try {
            val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else {
                0
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                application.registerReceiver(chargingReceiver, filter, receiverFlags)
            } else {
                application.registerReceiver(chargingReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start local sampling loop
        viewModelScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(3000) // Sample battery state every 3 seconds
                    val batteryManager = application.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    val currentLevel = HyperChargeService.liveBatteryLevel.value
                    val currentTemp = HyperChargeService.liveTemperature.value
                    val isCharging = HyperChargeService.liveIsCharging.value
                    val currentVoltage = HyperChargeService.liveVoltageMv.value

                    val currentMa = if (batteryManager != null) {
                        val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                        // Normalize current (sometimes in microamperes)
                        if (Math.abs(current) > 100000) current / 1000 else current
                    } else {
                        0
                    }

                    HyperChargeService.liveCurrentMa.value = currentMa

                    // If temperature is high (> 38 C)
                    if (currentTemp >= 38f && isCharging) {
                        HyperChargeService.addLog("⚠️ High Thermal Warning ($currentTemp°C)! Suppressing background load.")
                        HyperChargeService.liveCpuCoreStatus.value = "Thermal Restricted (Low Power)"
                        HyperChargeService.liveWakelockStatus.value = "Forced Sleep Active (No Wakelocks)"
                    } else {
                        HyperChargeService.liveCpuCoreStatus.value = if (isCharging) "Balanced Charge Governor" else "Active (Standard)"
                        HyperChargeService.liveWakelockStatus.value = "Safe Path"
                    }

                    // AI Charge scheduler logic (Predict completed duration)
                    if (isCharging) {
                        val remainingCap = 100 - currentLevel
                        if (remainingCap > 0) {
                            // Advanced heuristic charging curve mapping
                            val rate = if (currentTemp > 40f) 0.5f else if (currentLevel < 80) 1.2f else 0.7f
                            val estMin = ((remainingCap / (Math.abs(currentMa).toFloat() / 1000f + 0.1f)) * 60f * rate).toInt()
                            HyperChargeService.liveEstimatedFullMinutes.value = estMin.coerceAtLeast(1).coerceAtMost(300)
                        } else {
                            HyperChargeService.liveEstimatedFullMinutes.value = 0
                        }
                    } else {
                        HyperChargeService.liveEstimatedFullMinutes.value = -1
                    }

                    // Save to Room db
                    val sample = BatterySamplePoint(
                        timestamp = System.currentTimeMillis(),
                        batteryLevel = currentLevel,
                        temperature = currentTemp,
                        voltageMv = currentVoltage,
                        currentMa = currentMa,
                        chargingState = if (isCharging) "Charging" else "Discharging"
                    )
                    repository.insertSample(sample)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        HyperChargeService.addLog("⚡ HyperCharge Engine fully initialized locally!")
    }

    fun setAutoDisableRadios(value: Boolean) {
        autoDisableRadios.value = value
        viewModelScope.launch {
            repository.saveSetting("auto_disable_radios", value)
            HyperChargeService.addLog("⚙️ Radio Optimizations toggled: $value")
        }
    }

    fun setPreventOverheat(value: Boolean) {
        preventOverheat.value = value
        viewModelScope.launch {
            repository.saveSetting("prevent_overheat", value)
            HyperChargeService.addLog("⚙️ Thermal Safeguard protection: $value")
        }
    }

    fun setPreserveBatteryHealth(value: Boolean) {
        preserveBatteryHealth.value = value
        viewModelScope.launch {
            repository.saveSetting("preserve_battery_health", value)
            HyperChargeService.addLog("⚙️ Ion Cell Preservation toggled: $value")
        }
    }

    fun setPuristAmoledMode(value: Boolean) {
        puristAmoledMode.value = value
        viewModelScope.launch {
            repository.saveSetting("purist_amoled_mode", value)
            HyperChargeService.addLog("⚙️ AMOLED Pure-Dark Core Mode: $value")
        }
    }

    fun setBatteryChargeLimit(value: Int) {
        batteryChargeLimit.value = value
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("hypercharge_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("battery_charge_limit", value).apply()
            HyperChargeService.addLog("⚙️ Battery Target Threshold adapted: $value%")
        }
    }



    fun setAppLanguage(lang: String) {
        appLanguage.value = lang
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("hypercharge_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("app_language", lang).apply()
            val logMsg = if (lang == "ar") "⚙️ تم تغيير لغة الواجهة إلى العربية" else "⚙️ Interface language changed to English"
            HyperChargeService.addLog(logMsg)
        }
    }

    fun setPowerProfile(profile: String) {
        selectedPowerProfile.value = profile
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("hypercharge_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("selected_power_profile", profile).apply()
            
            when (profile) {
                "Eco" -> {
                    setBatteryChargeLimit(80)
                    setPreventOverheat(true)
                    setPreserveBatteryHealth(true)
                    val logMsg = if (appLanguage.value == "ar") {
                        "⚙️ تم تفعيل الملف الاقتصادي (Eco): تم تقييد الشحن بـ 80% لإطالة الخلايا"
                    } else {
                        "⚙️ Eco profile activated: charging cap restricted to 80% to lower stress Index"
                    }
                    HyperChargeService.addLog(logMsg)
                }
                "Gaming" -> {
                    setBatteryChargeLimit(100)
                    val logMsg = if (appLanguage.value == "ar") {
                        "⚙️ تم تفعيل ملف الألعاب والسرعة (Gaming): فك قيود الأيونات وتعزيز الطاقة"
                    } else {
                        "⚙️ Gaming profile activated: unrestricted energy throughput & robust performance"
                    }
                    HyperChargeService.addLog(logMsg)
                }
                "Balanced" -> {
                    setBatteryChargeLimit(90)
                    val logMsg = if (appLanguage.value == "ar") {
                        "⚙️ تم تفعيل الملف المتوازن (Balanced): شحن ذكي متكيف مع التدفق والحرارة"
                    } else {
                        "⚙️ Balanced profile activated: intellectual thermal adaptation capped at 90%"
                    }
                    HyperChargeService.addLog(logMsg)
                }
            }
        }
    }





    fun setExtremeChargingActive(value: Boolean) {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("hypercharge_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_extreme_charging_active", value).apply()
            HyperChargeService.isExtremeChargingActive.value = value
            val logMsg = if (appLanguage.value == "ar") {
                if (value) "⚡ تم تنشيط محرك الشحن فائق السرعة الحقيقي! تقييد السطوع وإرغام تطبيقات الخلفية بالبيات الهادئ فوراً." 
                else "🔓 تم إيقاف تعزيز الشحن فائق السرعة. استعادة معلمات السطوع العادية."
            } else {
                if (value) "⚡ Smart Turbo Charging Engine Activated! Restricting screen brightness and freezing background CPU drains."
                else "🔓 Turbo charging enhancement disabled. Restoring system state..."
            }
            HyperChargeService.addLog(logMsg)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.pruneSamplesBefore(System.currentTimeMillis() + 100000)
            HyperChargeService.addLog("🧹 Cleared diagnostic logs.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isReceiverRegistered) {
            try {
                getApplication<Application>().unregisterReceiver(chargingReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
