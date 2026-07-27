package com.fitworkup.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.service.WorkoutSensorService
import com.fitworkup.app.service.WorkoutState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val timeFormatted: String = "00:00:00",
    val distanceKmFormatted: String = "0,00 km",
    val paceFormatted: String = "--'--\" /km",
    val steps: Int = 0,
    val fitCoinsEarned: Int = 0,
    val xpEarned: Int = 0,
    val xpProgress: Float = 0f,
    val currentLocation: LatLng? = null,
    val pathPoints: List<LatLng> = emptyList(),
    val isTracking: Boolean = false,
    val isPaused: Boolean = false
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var workoutService: WorkoutSensorService? = null
    private var timerJob: Job? = null
    private var secondsElapsed: Long = 0

    fun onServiceConnected(service: WorkoutSensorService) {
        workoutService = service
        startObserveSensorState()
    }

    fun startWorkout(context: Context? = null, workoutType: String? = null) {
        val ctx = context ?: appContext
        val intent = Intent(ctx, WorkoutSensorService::class.java).apply {
            action = WorkoutSensorService.ACTION_START
        }
        ContextCompat.startForegroundService(ctx, intent)

        secondsElapsed = 0
        _uiState.value = WorkoutUiState(isTracking = true, isPaused = false)
        startTimer()
    }

    fun pauseWorkout() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isPaused = true)
    }

    fun resumeWorkout() {
        _uiState.value = _uiState.value.copy(isPaused = false)
        startTimer()
    }

    fun stopWorkout(context: Context? = null) {
        val ctx = context ?: appContext
        val intent = Intent(ctx, WorkoutSensorService::class.java).apply {
            action = WorkoutSensorService.ACTION_STOP
        }
        ctx.stopService(intent)

        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTracking = false, isPaused = false)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!_uiState.value.isPaused) {
                    secondsElapsed++
                    val currentSensorState = workoutService?.workoutState?.value ?: WorkoutState()
                    updateUiMetrics(currentSensorState)
                }
            }
        }
    }

    private fun startObserveSensorState() {
        viewModelScope.launch {
            workoutService?.workoutState?.collect { sensorState ->
                if (!_uiState.value.isPaused) {
                    updateUiMetrics(sensorState)
                }
            }
        }
    }

    private fun updateUiMetrics(sensorState: WorkoutState) {
        // Formata Tempo
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val seconds = secondsElapsed % 60
        val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // Formata Distância
        val distanceKm = sensorState.distanceMeters / 1000f
        val distanceStr = String.format("%.2f km", distanceKm)

        // Cálculo de Pace
        val paceStr = if (distanceKm >= 0.02f && secondsElapsed > 0) {
            val paceSecondsTotal = (secondsElapsed / distanceKm).toInt()
            val paceMin = paceSecondsTotal / 60
            val paceSec = paceSecondsTotal % 60

            if (paceMin in 1..19) {
                String.format("%d'%02d\" /km", paceMin, paceSec)
            } else {
                "--'--\" /km"
            }
        } else {
            "--'--\" /km"
        }

        // Gamificação
        val fitCoins = (distanceKm * 6).toInt()
        val xp = (distanceKm * 100 + sensorState.steps * 0.1f).toInt()
        val xpProgress = ((xp % 500) / 500f).coerceIn(0f, 1f)

        // Localização e Caminho
        val newLatLng = extractLatLngFromState(sensorState)

        val updatedPath = if (newLatLng != null && (_uiState.value.pathPoints.isEmpty() || _uiState.value.pathPoints.last() != newLatLng)) {
            _uiState.value.pathPoints + newLatLng
        } else {
            _uiState.value.pathPoints
        }

        _uiState.value = _uiState.value.copy(
            timeFormatted = timeStr,
            distanceKmFormatted = distanceStr,
            paceFormatted = paceStr,
            steps = sensorState.steps,
            fitCoinsEarned = fitCoins,
            xpEarned = xp,
            xpProgress = xpProgress,
            currentLocation = newLatLng ?: _uiState.value.currentLocation,
            pathPoints = updatedPath
        )
    }

    private fun extractLatLngFromState(sensorState: WorkoutState): LatLng? {
        return if (sensorState.latitude != 0.0 && sensorState.longitude != 0.0) {
            LatLng(sensorState.latitude, sensorState.longitude)
        } else {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}