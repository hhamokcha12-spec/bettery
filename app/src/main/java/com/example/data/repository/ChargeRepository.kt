package com.example.data.repository

import com.example.data.local.ChargeDao
import com.example.data.local.ChargeSession
import com.example.data.local.BatterySamplePoint
import com.example.data.local.ProfileSetting
import kotlinx.coroutines.flow.Flow

class ChargeRepository(private val chargeDao: ChargeDao) {
    val allSessions: Flow<List<ChargeSession>> = chargeDao.getAllSessions()
    val recentSamples: Flow<List<BatterySamplePoint>> = chargeDao.getRecentSamples()

    suspend fun insertSession(session: ChargeSession) {
        chargeDao.insertSession(session)
    }

    suspend fun insertSample(sample: BatterySamplePoint) {
        chargeDao.insertSample(sample)
    }

    suspend fun pruneSamplesBefore(timestamp: Long) {
        chargeDao.pruneSamples(timestamp)
    }

    suspend fun getSetting(key: String, defaultValue: Boolean): Boolean {
        return chargeDao.getSetting(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: Boolean) {
        chargeDao.insertSetting(ProfileSetting(key, value))
    }
}
