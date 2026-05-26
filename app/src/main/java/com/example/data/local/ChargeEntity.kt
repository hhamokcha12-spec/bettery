package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_sessions")
data class ChargeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val startBatteryLevel: Int,
    val endBatteryLevel: Int,
    val maxTemp: Float,
    val chargerType: String,
    val energyUsedWh: Float,
    val averageCurrentMa: Float,
    val healthAtSession: String
)

@Entity(tableName = "battery_sample_points")
data class BatterySamplePoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val batteryLevel: Int,
    val temperature: Float,
    val voltageMv: Int,
    val currentMa: Int,
    val chargingState: String
)

@Entity(tableName = "profile_settings")
data class ProfileSetting(
    @PrimaryKey val key: String,
    val value: Boolean
)
