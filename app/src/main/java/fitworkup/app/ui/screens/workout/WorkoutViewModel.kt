package com.fitworkup.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
    val isPaused: Boolean = false,
    val isSubmitting: Boolean = false,
    val permissionNeeded: Boolean = false,
    val errorMessage: String? = null
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

    private fun hasRequiredPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val activityRec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return fineLocation && activityRec
    }

    fun startWorkout(context: Context? = null, workoutType: String? = null) {
        val ctx = context ?: appContext

        if (!hasRequiredPermissions(ctx)) {
            _uiState.value = _uiState.value.copy(
                permissionNeeded = true,
                errorMessage = "Permissões de Localização e Atividade Física são necessárias para iniciar."
            )
            return
        }

        try {
            val intent = Intent(ctx, WorkoutSensorService::class.java).apply {
                action = WorkoutSensorService.ACTION_START
            }
            ContextCompat.startForegroundService(ctx, intent)

            secondsElapsed = 0
            _uiState.value = _uiState.value.copy(
                isTracking = true,
                isPaused = false,
                permissionNeeded = false,
                errorMessage = null
            )
            startTimer()
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                permissionNeeded = true,
                errorMessage = "Erro de segurança: Conceda as permissões necessárias no sistema."
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                errorMessage = "Erro ao iniciar monitoramento: ${e.localizedMessage}"
            )
        }
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
        try {
            val intent = Intent(ctx, WorkoutSensorService::class.java).apply {
                action = WorkoutSensorService.ACTION_STOP
            }
            ctx.stopService(intent)
        } catch (e: Exception) {
            // Log do erro ao encerrar serviço
        }

        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTracking = false, isPaused = false)
    }

    fun finishWorkout(context: Context? = null, workoutType: String = "CAMINHADA") {
        val ctx = context ?: appContext
        _uiState.value = _uiState.value.copy(isSubmitting = true)

        viewModelScope.launch {
            try {
                stopWorkout(ctx)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isTracking = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Erro ao finalizar o treino: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, permissionNeeded = false)
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
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val seconds = secondsElapsed % 60
        val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val distanceKm = sensorState.distanceMeters / 1000f
        val distanceStr = String.format("%.2f km", distanceKm)

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

        val fitCoins = (distanceKm * 6).toInt()
        val xp = (distanceKm * 100 + sensorState.steps * 0.1f).toInt()
        val xpProgress = ((xp % 500) / 500f).coerceIn(0f, 1f)

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