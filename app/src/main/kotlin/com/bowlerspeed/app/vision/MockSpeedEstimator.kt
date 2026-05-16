package com.bowlerspeed.app.vision

import com.bowlerspeed.app.data.CalibrationEntity
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

data class SpeedEstimate(
    val speedKph: Double,
    val confidence: Double
)

object MockSpeedEstimator {
    fun estimate(videoFile: File, calibration: CalibrationEntity?): SpeedEstimate {
        val sizeFactor = (videoFile.length() % 11L).toDouble() / 10.0
        val calibrationPenalty = calibration?.let {
            val heightPenalty = abs(it.cameraHeightM - 1.25) * 0.06
            val distancePenalty = abs(it.distanceBehindStumpsM - 8.0) * 0.025
            heightPenalty + distancePenalty
        } ?: 0.18

        val speed = 118.0 + (sizeFactor * 9.0) + Random.nextDouble(-6.0, 6.0)
        val confidence = (0.84 - calibrationPenalty + Random.nextDouble(-0.06, 0.04))
            .coerceIn(0.38, 0.96)

        return SpeedEstimate(
            speedKph = speed,
            confidence = confidence
        )
    }
}