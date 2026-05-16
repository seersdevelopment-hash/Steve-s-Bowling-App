package com.bowlerspeed.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochMillis: Long,
    val location: String,
    val coachName: String,
    val notes: String
)

@Entity(
    tableName = "deliveries",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class DeliveryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val ballNumber: Int,
    val speedKph: Double,
    val confidence: Double,
    val videoPath: String,
    val recordedAtEpochMillis: Long
)

@Entity(
    tableName = "calibrations",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class CalibrationEntity(
    @PrimaryKey val sessionId: Long,
    val cameraHeightM: Double,
    val distanceBehindStumpsM: Double,
    val pitchScaleMetersPerPixel: Double,
    val placementStatus: String
)