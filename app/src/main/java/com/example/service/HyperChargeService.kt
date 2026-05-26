package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow

class HyperChargeService : Service() {

    companion object {
        // Reactive streams of real-time diagnostics so UI can read it directly!
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

        fun addLog(msg: String) {
            val list = liveActivityLogs.value.toMutableList()
            list.add(0, "[${System.currentTimeMillis() % 100000}] $msg")
            if (list.size > 100) list.removeAt(list.size - 1)
            liveActivityLogs.value = list
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
