package com.bowlerspeed.app.ui

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bowlerspeed.app.data.BowlerSpeedDatabase
import com.bowlerspeed.app.data.BowlerSpeedRepository
import com.bowlerspeed.app.data.CalibrationEntity
import com.bowlerspeed.app.data.DeliveryEntity
import com.bowlerspeed.app.data.SessionEntity
import com.bowlerspeed.app.vision.MockSpeedEstimator
import java.io.File
import kotlinx.coroutines.launch

enum class BowlerScreen {
    Home,
    Setup,
    Calibration,
    Recording,
    Result,
    Summary,
    History
}

data class SessionDraft(
    val coachName: String = "",
    val location: String = "",
    val notes: String = ""
)

data class CalibrationDraft(
    val cameraHeightM: Float = 1.25f,
    val distanceBehindStumpsM: Float = 8.0f,
    val pitchScaleMetersPerPixel: Float = 0.034f
) {
    val placementStatus: String
        get() = when {
            cameraHeightM < 0.9f -> "Camera is too low for a stable ball track"
            cameraHeightM > 1.7f -> "Camera is too high for the recommended tripod setup"
            distanceBehindStumpsM < 5.5f -> "Move the phone farther behind the stumps"
            distanceBehindStumpsM > 11.0f -> "Move the phone closer to the stumps"
            else -> "Camera placement is inside the recommended setup window"
        }

    val isRecommended: Boolean
        get() = placementStatus.startsWith("Camera placement")
}

data class BowlerSpeedUiState(
    val screen: BowlerScreen = BowlerScreen.Home,
    val sessions: List<SessionEntity> = emptyList(),
    val currentSession: SessionEntity? = null,
    val deliveries: List<DeliveryEntity> = emptyList(),
    val lastDelivery: DeliveryEntity? = null,
    val sessionDraft: SessionDraft = SessionDraft(),
    val calibrationDraft: CalibrationDraft = CalibrationDraft(),
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
) {
    val nextBallNumber: Int = deliveries.size + 1
}

class BowlerSpeedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BowlerSpeedRepository(
        BowlerSpeedDatabase.get(application).dao()
    )

    var uiState = androidx.compose.runtime.mutableStateOf(BowlerSpeedUiState())
        private set

    init {
        viewModelScope.launch {
            repository.observeSessions().collect { sessions ->
                uiState.value = uiState.value.copy(sessions = sessions)
            }
        }
    }

    fun updateSessionDraft(draft: SessionDraft) {
        uiState.value = uiState.value.copy(sessionDraft = draft, errorMessage = null)
    }

    fun updateCalibrationDraft(draft: CalibrationDraft) {
        uiState.value = uiState.value.copy(calibrationDraft = draft, errorMessage = null)
    }

    fun startSession() {
        val draft = uiState.value.sessionDraft
        viewModelScope.launch {
            val sessionId = repository.createSession(
                coachName = draft.coachName.ifBlank { "Coach" },
                location = draft.location.ifBlank { "Training ground" },
                notes = draft.notes
            )
            openSession(sessionId, BowlerScreen.Setup)
            uiState.value = uiState.value.copy(sessionDraft = SessionDraft())
        }
    }

    fun saveCalibration() {
        val session = uiState.value.currentSession ?: return
        val draft = uiState.value.calibrationDraft
        viewModelScope.launch {
            repository.saveCalibration(
                CalibrationEntity(
                    sessionId = session.id,
                    cameraHeightM = draft.cameraHeightM.toDouble(),
                    distanceBehindStumpsM = draft.distanceBehindStumpsM.toDouble(),
                    pitchScaleMetersPerPixel = draft.pitchScaleMetersPerPixel.toDouble(),
                    placementStatus = draft.placementStatus
                )
            )
            uiState.value = uiState.value.copy(screen = BowlerScreen.Recording)
        }
    }

    fun openSession(sessionId: Long, destination: BowlerScreen = BowlerScreen.Summary) {
        viewModelScope.launch {
            val session = repository.getSession(sessionId)
            val deliveries = repository.getDeliveries(sessionId)
            val calibration = repository.getCalibration(sessionId)
            uiState.value = uiState.value.copy(
                screen = destination,
                currentSession = session,
                deliveries = deliveries,
                lastDelivery = deliveries.lastOrNull(),
                calibrationDraft = calibration?.let {
                    CalibrationDraft(
                        cameraHeightM = it.cameraHeightM.toFloat(),
                        distanceBehindStumpsM = it.distanceBehindStumpsM.toFloat(),
                        pitchScaleMetersPerPixel = it.pitchScaleMetersPerPixel.toFloat()
                    )
                } ?: uiState.value.calibrationDraft,
                errorMessage = null
            )
        }
    }

    fun goTo(screen: BowlerScreen) {
        uiState.value = uiState.value.copy(screen = screen, errorMessage = null)
    }

    fun newBall() {
        uiState.value = uiState.value.copy(screen = BowlerScreen.Recording, lastDelivery = null)
    }

    fun createVideoFile(sessionId: Long, ballNumber: Int): File {
        val moviesDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val sessionDir = File(moviesDir, "session_$sessionId").apply { mkdirs() }
        return File(sessionDir, "ball_${ballNumber}_${System.currentTimeMillis()}.mp4")
    }

    fun processRecordedClip(videoFile: File) {
        val session = uiState.value.currentSession ?: return
        uiState.value = uiState.value.copy(isProcessing = true, errorMessage = null)
        viewModelScope.launch {
            val calibration = repository.getCalibration(session.id)
            val ballNumber = repository.nextBallNumber(session.id)
            val estimate = MockSpeedEstimator.estimate(videoFile, calibration)
            val delivery = DeliveryEntity(
                sessionId = session.id,
                ballNumber = ballNumber,
                speedKph = estimate.speedKph,
                confidence = estimate.confidence,
                videoPath = videoFile.absolutePath,
                recordedAtEpochMillis = System.currentTimeMillis()
            )
            repository.saveDelivery(delivery)
            val deliveries = repository.getDeliveries(session.id)
            uiState.value = uiState.value.copy(
                screen = BowlerScreen.Result,
                deliveries = deliveries,
                lastDelivery = deliveries.lastOrNull(),
                isProcessing = false
            )
        }
    }

    fun showRecordingError(message: String) {
        uiState.value = uiState.value.copy(
            isProcessing = false,
            errorMessage = message
        )
    }
}