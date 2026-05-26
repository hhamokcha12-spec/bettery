package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeDao {
    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargeSession)

    @Query("SELECT * FROM battery_sample_points ORDER BY timestamp DESC LIMIT 500")
    fun getRecentSamples(): Flow<List<BatterySamplePoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: BatterySamplePoint)

    @Query("DELETE FROM battery_sample_points WHERE timestamp < :threshold")
    suspend fun pruneSamples(threshold: Long)

    @Query("SELECT * FROM profile_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): ProfileSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: ProfileSetting)
}
