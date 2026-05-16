package com.bowlerspeed.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BowlerSpeedDao {
    @Query("SELECT * FROM sessions ORDER BY dateEpochMillis DESC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM deliveries WHERE sessionId = :sessionId ORDER BY ballNumber ASC")
    suspend fun getDeliveries(sessionId: Long): List<DeliveryEntity>

    @Query("SELECT COUNT(*) FROM deliveries WHERE sessionId = :sessionId")
    suspend fun deliveryCount(sessionId: Long): Int

    @Query("SELECT * FROM calibrations WHERE sessionId = :sessionId")
    suspend fun getCalibration(sessionId: Long): CalibrationEntity?

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalibration(calibration: CalibrationEntity)

    @Insert
    suspend fun insertDelivery(delivery: DeliveryEntity): Long
}