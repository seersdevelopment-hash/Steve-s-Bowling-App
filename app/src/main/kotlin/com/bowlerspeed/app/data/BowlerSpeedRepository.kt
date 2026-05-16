package com.bowlerspeed.app.data

import kotlinx.coroutines.flow.Flow

class BowlerSpeedRepository(private val dao: BowlerSpeedDao) {
    fun observeSessions(): Flow<List<SessionEntity>> = dao.observeSessions()

    suspend fun createSession(coachName: String, location: String, notes: String): Long {
        return dao.insertSession(
            SessionEntity(
                dateEpochMillis = System.currentTimeMillis(),
                coachName = coachName.trim(),
                location = location.trim(),
                notes = notes.trim()
            )
        )
    }

    suspend fun saveCalibration(calibration: CalibrationEntity) {
        dao.upsertCalibration(calibration)
    }

    suspend fun getSession(sessionId: Long): SessionEntity? = dao.getSession(sessionId)

    suspend fun getCalibration(sessionId: Long): CalibrationEntity? = dao.getCalibration(sessionId)

    suspend fun getDeliveries(sessionId: Long): List<DeliveryEntity> = dao.getDeliveries(sessionId)

    suspend fun nextBallNumber(sessionId: Long): Int = dao.deliveryCount(sessionId) + 1

    suspend fun saveDelivery(delivery: DeliveryEntity): Long = dao.insertDelivery(delivery)
}