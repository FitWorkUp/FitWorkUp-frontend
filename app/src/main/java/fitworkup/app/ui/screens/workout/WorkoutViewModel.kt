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
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val timeFormatted: String = "00:00:00",
    val distanceKmFormatted: String = "0,00 km",
    val paceFormatted: String = "--'--\" /km",
    val speedFormatted: String = "0,0 km/h",
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
    val gpsReady: Boolean = false,
    val stepSensorAvailable: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var workoutService: WorkoutSensorService? = null
    private var sensorStateJob: Job? = null
    private var lastPathDistanceMeters = 0f

    fun onServiceConnected(service: WorkoutSensorService) {
        workoutService = service
        sensorStateJob?.cancel()
        sensorStateJob = viewModelScope.launch {
            service.workoutState.collect(::updateUiMetrics)
        }
    }

    fun onServiceDisconnected() {
        sensorStateJob?.cancel()
        sensorStateJob = null
        workoutService = null
    }

    fun startWorkout(
        context: Context? = null,
        workoutType: String? = "CAMINHADA"
    ) {
        val ctx = context ?: appContext
        if (!hasRequiredPermissions(ctx)) {
            _uiState.value = _uiState.value.copy(
                permissionNeeded = true,
                errorMessage =
                    "Permissões de localização e atividade física são necessárias."
            )
            return
        }

        lastPathDistanceMeters = 0f
        _uiState.value = WorkoutUiState(isTracking = true)

        runCatching {
            val intent = Intent(
                ctx,
                WorkoutSensorService::class.java
            ).apply {
                action = WorkoutSensorService.ACTION_START
                putExtra(
                    WorkoutSensorService.EXTRA_WORKOUT_TYPE,
                    workoutType ?: "CAMINHADA"
                )
            }
            ContextCompat.startForegroundService(ctx, intent)
        }.onFailure(::showStartError)
    }

    fun pauseWorkout() {
        sendServiceAction(WorkoutSensorService.ACTION_PAUSE)
    }

    fun resumeWorkout() {
        sendServiceAction(WorkoutSensorService.ACTION_RESUME)
    }

    fun stopWorkout(context: Context? = null) {
        sendServiceAction(
            WorkoutSensorService.ACTION_STOP,
            context ?: appContext
        )
    }

    fun finishWorkout(
        context: Context? = null,
        workoutType: String = "CAMINHADA"
    ) {
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        stopWorkout(context)
        _uiState.value = _uiState.value.copy(
            isSubmitting = false,
            isTracking = false,
            isPaused = false
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            permissionNeeded = false
        )
    }

    private fun sendServiceAction(
        action: String,
        context: Context = appContext
    ) {
        runCatching {
            context.startService(
                Intent(
                    context,
                    WorkoutSensorService::class.java
                ).apply {
                    this.action = action
                }
            )
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                errorMessage = error.localizedMessage
                    ?: "Não foi possível controlar o treino."
            )
        }
    }

    private fun updateUiMetrics(sensorState: WorkoutState) {
        val elapsedSeconds = sensorState.elapsedMillis / 1_000L
        val hours = elapsedSeconds / 3_600L
        val minutes = (elapsedSeconds % 3_600L) / 60L
        val seconds = elapsedSeconds % 60L

        val distanceKm = sensorState.distanceMeters / 1_000f
        val newLocation = extractLatLng(sensorState)
        val oldState = _uiState.value

        val shouldAppendPath =
            newLocation != null &&
                    sensorState.gpsReady &&
                    sensorState.distanceMeters > lastPathDistanceMeters &&
                    oldState.pathPoints.lastOrNull() != newLocation

        val updatedPath = if (shouldAppendPath) {
            lastPathDistanceMeters = sensorState.distanceMeters
            oldState.pathPoints + newLocation
        } else {
            oldState.pathPoints
        }

        val pace = if (
            distanceKm >= MIN_DISTANCE_FOR_PACE_KM &&
            elapsedSeconds > 0L
        ) {
            formatPace(elapsedSeconds, distanceKm)
        } else {
            "--'--\" /km"
        }

        val fitCoins = (distanceKm * FITCOINS_PER_KM).toInt()
        val xp = (
                distanceKm * XP_PER_KM +
                        sensorState.steps * XP_PER_STEP
                ).toInt()

        _uiState.value = oldState.copy(
            timeFormatted = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            ),
            distanceKmFormatted = String.format(
                Locale.getDefault(),
                "%.2f km",
                distanceKm
            ),
            paceFormatted = pace,
            speedFormatted = String.format(
                Locale.getDefault(),
                "%.1f km/h",
                sensorState.speedMps * 3.6f
            ),
            steps = sensorState.steps,
            fitCoinsEarned = fitCoins,
            xpEarned = xp,
            xpProgress = ((xp % 500) / 500f).coerceIn(0f, 1f),
            currentLocation = newLocation ?: oldState.currentLocation,
            pathPoints = updatedPath,
            isTracking = sensorState.isTracking,
            isPaused = sensorState.isPaused,
            gpsReady = sensorState.gpsReady,
            stepSensorAvailable = sensorState.stepSensorAvailable,
            errorMessage = sensorState.errorMessage
                ?: oldState.errorMessage
        )
    }

    private fun formatPace(
        elapsedSeconds: Long,
        distanceKm: Float
    ): String {
        val total = (elapsedSeconds / distanceKm).toInt()
        val minutes = total / 60
        val seconds = total % 60

        return if (minutes in 1..30) {
            String.format(
                Locale.getDefault(),
                "%d'%02d\" /km",
                minutes,
                seconds
            )
        } else {
            "--'--\" /km"
        }
    }

    private fun extractLatLng(state: WorkoutState): LatLng? {
        val latitude = state.latitude ?: return null
        val longitude = state.longitude ?: return null
        return LatLng(latitude, longitude)
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        val locationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val activityGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED

        return locationGranted && activityGranted
    }

    private fun showStartError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            errorMessage =
                "Erro ao iniciar monitoramento: ${error.localizedMessage}"
        )
    }

    override fun onCleared() {
        sensorStateJob?.cancel()
        workoutService = null
        super.onCleared()
    }

    companion object {
        private const val FITCOINS_PER_KM = 10f
        private const val XP_PER_KM = 100f
        private const val XP_PER_STEP = 0.1f
        private const val MIN_DISTANCE_FOR_PACE_KM = 0.05f
    }
}
