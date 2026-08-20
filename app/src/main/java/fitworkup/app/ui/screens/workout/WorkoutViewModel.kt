package com.fitworkup.app.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.domain.repository.ActivityRepository
import com.fitworkup.app.domain.model.RoutePoint
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

    fun configureWorkout(goalKm: Double?, groupSessionId: Long?) {
        val sanitizedGoal = goalKm?.takeIf { it.isFinite() && it > 0.0 }
        _uiState.update {
            it.copy(
                targetDistanceKm = sanitizedGoal,
                groupSessionId = groupSessionId?.takeIf { id -> id > 0L }
            )
        }
    }

    fun onServiceConnected(service: WorkoutSensorService) {
        viewModelScope.launch {
            service.workoutState.collect { state ->
                processSensorState(state)
            }
        }
    }

    private fun processSensorState(sensorState: WorkoutState) {
        val rawDistanceKm = (sensorState.distanceMeters / 1000.0)
        val speedKmH = (sensorState.speedMps * 3.6)

        val newStepCount = sensorState.steps
        val stepDelta = newStepCount - lastProcessedSteps
        val isWalking = stepDelta > 0 || newStepCount > 0

        val currentState = _uiState.value

        // Validação de segurança do sinal de GPS e movimento real
        val isGpsValid = sensorState.gpsAccuracyMeters <= 15.0f
        val shouldUpdatePathAndDistance = isWalking && isGpsValid

        // Preserva o path e a distância anteriores se o usuário estiver parado (GPS Drift)
        val safeDistanceKm = if (shouldUpdatePathAndDistance || currentState.totalDistanceKm == 0f) {
            rawDistanceKm.toFloat()
        } else {
            currentState.totalDistanceKm
        }

        val safePathPoints = if (shouldUpdatePathAndDistance || currentState.pathPoints.isEmpty()) {
            sensorState.pathPoints
        } else {
            currentState.pathPoints
        }

        if (stepDelta > 0) {
            val eval = antiFraudEvaluator.evaluateStepDelta(newStepCount, speedKmH)
            lastProcessedSteps = newStepCount

            val newAccepted = if (eval.isAccepted) currentState.acceptedSteps + stepDelta else currentState.acceptedSteps
            val newHeld = if (!eval.isAccepted) currentState.heldSteps + stepDelta else currentState.heldSteps

            val updatedReasons = currentState.fraudReasons.toMutableList()
            if (!eval.reason.isNullOrBlank() && !updatedReasons.contains(eval.reason)) {
                updatedReasons.add(eval.reason)
            }

            _uiState.update {
                it.copy(
                    isTracking = sensorState.isTracking,
                    totalSteps = newStepCount,
                    acceptedSteps = newAccepted,
                    heldSteps = newHeld,
                    totalDistanceKm = safeDistanceKm,
                    averageSpeedKmH = speedKmH.toFloat(),
                    durationSeconds = sensorState.durationSeconds,
                    gpsAccuracyMeters = sensorState.gpsAccuracyMeters,
                    riskScore = currentState.riskScore + eval.riskDelta,
                    fraudReasons = updatedReasons,
                    currentLocation = sensorState.currentLocation,
                    pathPoints = safePathPoints
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isTracking = sensorState.isTracking,
                    totalSteps = newStepCount,
                    totalDistanceKm = safeDistanceKm,
                    averageSpeedKmH = speedKmH.toFloat(),
                    durationSeconds = sensorState.durationSeconds,
                    gpsAccuracyMeters = sensorState.gpsAccuracyMeters,
                    currentLocation = sensorState.currentLocation,
                    pathPoints = safePathPoints
                )
            }
        }
    }

    fun finishWorkout(activityType: String) {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Validação e Sanitização de Doutrina LGPD / Zero Trust antes do envio ao backend
                val sanitizedDistance = if (state.totalDistanceKm.isNaN() || state.totalDistanceKm < 0) 0.0 else state.totalDistanceKm.toDouble()
                val sanitizedSpeed = if (state.averageSpeedKmH.isNaN() || state.averageSpeedKmH < 0) 0.0 else state.averageSpeedKmH.toDouble()

                val request = ActivityRequest(
                    type = activityType,
                    distanceKm = sanitizedDistance,
                    steps = state.totalSteps,
                    avgSpeed = sanitizedSpeed,
                    acceptedSteps = state.acceptedSteps,
                    heldSteps = state.heldSteps,
                    riskScore = state.riskScore,
                    fraudReasons = state.fraudReasons,
                    routePoints = state.pathPoints.map { location ->
                        RoutePoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = location.time,
                            accuracyMeters = location.accuracy
                        )
                    },
                    groupSessionId = state.groupSessionId
                )

                val result = activityRepository.registerActivity(request)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionSuccess = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionSuccess = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Falha ao registrar o treino."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = false,
                        errorMessage = "Erro de conexão/processamento: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
