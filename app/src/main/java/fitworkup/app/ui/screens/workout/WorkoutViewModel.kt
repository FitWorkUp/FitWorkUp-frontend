package com.fitworkup.app.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun onStepEvaluated(isAccepted: Boolean, currentRiskScore: Int, reason: String?, distanceKm: Double, avgSpeed: Double) {
        val currentState = _uiState.value
        val newAccepted = if (isAccepted) currentState.acceptedSteps + 1 else currentState.acceptedSteps
        val newHeld = if (!isAccepted) currentState.heldSteps + 1 else currentState.heldSteps

        val updatedReasons = currentState.fraudReasons.toMutableList()
        if (!reason.isNullOrBlank() && !updatedReasons.contains(reason)) {
            updatedReasons.add(reason)
        }

        _uiState.value = currentState.copy(
            totalSteps = newAccepted + newHeld,
            acceptedSteps = newAccepted,
            heldSteps = newHeld,
            distanceKm = distanceKm,
            avgSpeedKmH = avgSpeed,
            riskScore = currentRiskScore,
            fraudReasons = updatedReasons
        )
    }

    fun finishWorkout(activityType: String) {
        val state = _uiState.value
        if (state.isSubmitting) return

        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = ActivityRequest(
                    type = activityType,
                    distanceKm = state.distanceKm,
                    steps = state.totalSteps,
                    avgSpeed = state.avgSpeedKmH,
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