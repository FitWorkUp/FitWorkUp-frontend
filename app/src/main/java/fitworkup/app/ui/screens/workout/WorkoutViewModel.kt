package com.fitworkup.app.ui.screens.workout

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.service.WorkoutSensorService
import com.fitworkup.app.service.WorkoutState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val timeFormatted: String = "00:00:00",
    val distanceKmFormatted: String = "0.00 km",
    val paceFormatted: String = "0'00\" /km",
    val steps: Int = 0,
    val fitCoinsEarned: Int = 0,
    val xpEarned: Int = 0,
    val xpProgress: Float = 0f,
    val isPaused: Boolean = false
)

class WorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var workoutService: WorkoutSensorService? = null
    private var isBound = false

    private var secondsElapsed = 0
    private var timerJob: Job? = null

    // Conecta a UI ao ForegroundService de Sensores
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WorkoutSensorService.LocalBinder
            workoutService = binder.getService()
            isBound = true

            // Escuta o StateFlow do serviço com os passos e GPS atualizados
            viewModelScope.launch {
                workoutService?.workoutState?.collect { sensorState ->
                    updateUiMetrics(sensorState)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            workoutService = null
            isBound = false
        }
    }

    fun startWorkout(context: Context) {
        val intent = Intent(context, WorkoutSensorService::class.java).apply {
            action = WorkoutSensorService.ACTION_START
        }
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

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

    fun stopWorkout(context: Context) {
        timerJob?.cancel()
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        val intent = Intent(context, WorkoutSensorService::class.java).apply {
            action = WorkoutSensorService.ACTION_STOP
        }
        context.startService(intent)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                secondsElapsed++
                val currentSensorState = workoutService?.workoutState?.value ?: WorkoutState()
                updateUiMetrics(currentSensorState)
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

        // ─── CÁLCULO DE PACE ROBUSTO (FUNCIONA EM EMULADOR E DISPOSITIVO REAL) ───
        // Exige apenas que o usuário tenha percorrido mais de 20 metros (0.02 km)
        val paceStr = if (distanceKm >= 0.02f && secondsElapsed > 0) {
            val paceSecondsTotal = (secondsElapsed / distanceKm).toInt()
            val paceMin = paceSecondsTotal / 60
            val paceSec = paceSecondsTotal % 60

            // Se o ritmo for mais rápido que 20 min/km (ritmo de caminhada humana), exibe o número.
            // Se a pessoa parar, o tempo vai subir, o Pace vai passar de 20'00" e voltará para --'--"
            if (paceMin in 1..19) {
                String.format("%d'%02d\" /km", paceMin, paceSec)
            } else {
                "--'--\" /km"
            }
        } else {
            "--'--\" /km" // Exibe traços antes de atingir 20 metros
        }

        // Gamificação (Regras RF03)
        val fitCoins = (distanceKm * 6).toInt()
        val xp = (distanceKm * 100 + sensorState.steps * 0.1f).toInt()
        val xpProgress = ((xp % 500) / 500f).coerceIn(0f, 1f)

        _uiState.value = _uiState.value.copy(
            timeFormatted = timeStr,
            distanceKmFormatted = distanceStr,
            paceFormatted = paceStr,
            steps = sensorState.steps,
            fitCoinsEarned = fitCoins,
            xpEarned = xp,
            xpProgress = xpProgress
        )
    }
}