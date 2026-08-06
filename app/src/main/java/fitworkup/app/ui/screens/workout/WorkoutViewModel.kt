package com.fitworkup.app.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.domain.security.StepAntiFraudEvaluator
import com.fitworkup.app.service.WorkoutSensorService
import com.fitworkup.app.service.WorkoutState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val antiFraudEvaluator: StepAntiFraudEvaluator
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var lastProcessedSteps: Int = 0

    fun onServiceConnected(service: WorkoutSensorService) {
        viewModelScope.launch {
            service.workoutState.collect { state ->
                // 💡 Corrigido: Encaminha para o processador anti-fraude em vez de sobrescrever direto
                processSensorState(state)
            }
        }
    }

    private fun processSensorState(sensorState: WorkoutState) {
        val distanceKm = (sensorState.distanceMeters / 1000.0)
        val speedKmH = (sensorState.speedMps * 3.6)

        val newStepCount = sensorState.steps
        val stepDelta = newStepCount - lastProcessedSteps

        if (stepDelta > 0) {
            val eval = antiFraudEvaluator.evaluateStepDelta(newStepCount, speedKmH)
            lastProcessedSteps = newStepCount

            val currentState = _uiState.value
            val newAccepted = if (eval.isAccepted) currentState.acceptedSteps + stepDelta else currentState.acceptedSteps
            val newHeld = if (!eval.isAccepted) currentState.heldSteps + stepDelta else currentState.heldSteps

            val updatedReasons = currentState.fraudReasons.toMutableList()
            if (!eval.reason.isNullOrBlank() && !updatedReasons.contains(eval.reason)) {
                updatedReasons.add(eval.reason)
            }

            _uiState.value = currentState.copy(
                isTracking = sensorState.isTracking,
                totalSteps = newStepCount,
                acceptedSteps = newAccepted,
                heldSteps = newHeld,
                totalDistanceKm = distanceKm.toFloat(),
                averageSpeedKmH = speedKmH.toFloat(),
                durationSeconds = sensorState.durationSeconds,
                gpsAccuracyMeters = sensorState.gpsAccuracyMeters,
                riskScore = currentState.riskScore + eval.riskDelta,
                fraudReasons = updatedReasons,
                currentLocation = sensorState.currentLocation,
                pathPoints = sensorState.pathPoints
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isTracking = sensorState.isTracking,
                totalSteps = newStepCount,
                totalDistanceKm = distanceKm.toFloat(),
                averageSpeedKmH = speedKmH.toFloat(),
                durationSeconds = sensorState.durationSeconds,
                gpsAccuracyMeters = sensorState.gpsAccuracyMeters,
                currentLocation = sensorState.currentLocation,
                pathPoints = sensorState.pathPoints
            )
        }
    }

    fun finishWorkout(activityType: String) {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = ActivityRequest(
                    type = activityType,
                    distanceKm = state.totalDistanceKm.toDouble(),
                    steps = state.totalSteps,
                    avgSpeed = state.averageSpeedKmH.toDouble(),
                    acceptedSteps = state.acceptedSteps,
                    heldSteps = state.heldSteps,
                    riskScore = state.riskScore,
                    fraudReasons = state.fraudReasons
                )

                val result = activityRepository.registerActivity(request)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionSuccess = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Falha ao registrar o treino."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionSuccess = false,
                    errorMessage = "Erro imprevisível: ${e.localizedMessage}"
                )
            }
        }
    }
}