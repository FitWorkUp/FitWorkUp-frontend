package com.fitworkup.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.Manifest

data class WorkoutState(
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val speedMps: Float = 0f,
    val isTracking: Boolean = false
)

class WorkoutSensorService : Service(), SensorEventListener {

    private val binder = LocalBinder()

    // Estado reativo exposto para a UI (Jetpack Compose / ViewModel)
    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    // Gerenciador do Sensor de Passos
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var initialStepCount: Int = -1 // Offset para calcular apenas passos da sessão atual

    // Gerenciador do GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null
    private var totalDistanceMeters: Float = 0f

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutSensorService = this@WorkoutSensorService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        initSensors()
        initLocationClient()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWorkout()
            ACTION_STOP -> stopWorkout()
        }
        return START_STICKY
    }

    // --- 1. CONFIGURAÇÃO DE SENSORES E GPS ---

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateDistance(location)
                }
            }
        }
    }

    // --- 2. CONTROLE DO FLUXO DO TREINO ---

    private fun startWorkout() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Registra sensor de passos
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        // Registra atualizações do GPS (10 em 10 segundos)
        startLocationUpdates()

        _workoutState.value = _workoutState.value.copy(isTracking = true)
    }

    private fun stopWorkout() {
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        _workoutState.value = _workoutState.value.copy(isTracking = false)
    }

    // --- 3. CALLBACKS DOS SENSORES (PASSOS E LOCALIZAÇÃO) ---

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0].toInt()

            // O SENSOR_TYPE_STEP_COUNTER retorna passos desde o boot.
            // Guardamos o primeiro valor para calcular apenas a diferença do treino.
            if (initialStepCount == -1) {
                initialStepCount = totalStepsSinceBoot
            }

            val sessionSteps = totalStepsSinceBoot - initialStepCount
            _workoutState.value = _workoutState.value.copy(steps = sessionSteps)
            updateNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Garante que só chama o GPS se a permissão foi mesmo concedida
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateDistance(newLocation: Location) {
        lastLocation?.let { previous ->
            val distance = previous.distanceTo(newLocation)
            if (distance > 2.0f) {
                totalDistanceMeters += distance

                _workoutState.value = _workoutState.value.copy(
                    distanceMeters = totalDistanceMeters,
                    speedMps = newLocation.speed // 👈 Pega a velocidade atual do GPS em m/s
                )
            }
        }
        lastLocation = newLocation
    }
    // --- 4. NOTIFICAÇÃO PERMANENTE (FOREGROUND) ---

    private fun buildNotification(): Notification {
        val state = _workoutState.value
        val kmText = String.format("%.2f km", state.distanceMeters / 1000)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitWorkUp - Treino em Andamento")
            .setContentText("Passos: ${state.steps} | Distância: $kmText")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Substitua pelo ícone do app
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoramento de Atividade Física",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "workout_sensor_channel"
        private const val NOTIFICATION_ID = 1001

        private const val LOCATION_INTERVAL_MS = 10000L // 10 segundos
        private const val LOCATION_FASTEST_INTERVAL_MS = 5000L // 5 segundos
    }
}