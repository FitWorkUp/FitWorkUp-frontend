package com.fitworkup.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.fitworkup.app.MainActivity
import com.fitworkup.app.domain.model.ActiveModifier
import com.fitworkup.app.domain.repository.StoreRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

data class WorkoutState(
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedMps: Float = 0f,
    val gpsAccuracyMeters: Float = 0f, // 💡 Adicionado para o status dinâmico do GPS
    val durationSeconds: Long = 0L,   // 💡 Adicionado para o cronômetro do treino
    val isTracking: Boolean = false,
    val currentLocation: Location? = null,
    val pathPoints: List<Location> = emptyList()
)

@AndroidEntryPoint
class WorkoutSensorService : Service(), SensorEventListener {

    @Inject
    lateinit var storeRepository: StoreRepository

    private val binder = LocalBinder()

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var initialStepCount: Int = -1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null
    private var totalDistanceMeters: Float = 0f
    private val pathPointsList = mutableListOf<Location>()
    private var lastStepDetectedAtMs: Long = 0L
    private var workoutGoalKm: Double? = null
    private var workoutGroupSessionId: Long? = null
    private var activeModifiers: List<ActiveModifier> = emptyList()

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

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
            ACTION_START -> {
                workoutGoalKm = intent.getDoubleExtra(EXTRA_GOAL_KM, Double.NaN)
                    .takeIf { it.isFinite() && it > 0.0 }
                workoutGroupSessionId = intent.getLongExtra(EXTRA_GROUP_SESSION_ID, 0L)
                    .takeIf { it > 0L }
                startWorkout()
            }
            ACTION_STOP -> stopWorkout()
        }
        return START_NOT_STICKY
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocationAndDistance(location)
                }
            }
        }
    }

    private fun startWorkout() {
        if (_workoutState.value.isTracking) return

        createNotificationChannel()

        pathPointsList.clear()
        totalDistanceMeters = 0f
        lastLocation = null
        initialStepCount = -1
        lastStepDetectedAtMs = 0L

        _workoutState.value = WorkoutState(
            isTracking = true,
            distanceMeters = 0f,
            durationSeconds = 0L,
            currentLocation = null,
            pathPoints = emptyList()
        )

        startTimer()
        loadActiveModifiers()

        val notification = buildNotification()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            serviceType
        )

        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        startLocationUpdates()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && _workoutState.value.isTracking) {
                delay(1000L)
                _workoutState.value = _workoutState.value.copy(
                    durationSeconds = _workoutState.value.durationSeconds + 1
                )
                updateNotification()
            }
        }
    }

    private fun loadActiveModifiers() {
        serviceScope.launch {
            storeRepository.getActiveModifiers().onSuccess { modifiers ->
                activeModifiers = modifiers
                updateNotification()
            }
        }
    }

    private fun stopWorkout() {
        timerJob?.cancel()
        _workoutState.value = _workoutState.value.copy(isTracking = false)

        stepCounterSensor?.let { sensor ->
            sensorManager.unregisterListener(this, sensor)
        }

        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        stopSelf()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!_workoutState.value.isTracking) return

        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0].toInt()

            if (initialStepCount == -1) {
                initialStepCount = totalStepsSinceBoot
            }

            val sessionSteps = totalStepsSinceBoot - initialStepCount
            if (sessionSteps > _workoutState.value.steps) {
                lastStepDetectedAtMs = SystemClock.elapsedRealtime()
            }
            _workoutState.value = _workoutState.value.copy(steps = sessionSteps)
            updateNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
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
            setMinUpdateDistanceMeters(1.0f)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateLocationAndDistance(newLocation: Location) {
        if (!_workoutState.value.isTracking) return

        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            newLocation.isMock
        } else {
            @Suppress("DEPRECATION")
            newLocation.isFromMockProvider
        }

        if (isMock) return

        val accuracy = if (newLocation.hasAccuracy()) newLocation.accuracy else 50f

        // Ignora leituras do GPS caso a precisão seja pior que 25 metros
        if (accuracy > MAX_ROUTE_ACCURACY_METERS) {
            _workoutState.value = _workoutState.value.copy(gpsAccuracyMeters = accuracy)
            return
        }

        val hasRecentStep = lastStepDetectedAtMs > 0L &&
            SystemClock.elapsedRealtime() - lastStepDetectedAtMs <= RECENT_STEP_WINDOW_MS
        if (!hasRecentStep) {
            _workoutState.value = _workoutState.value.copy(
                gpsAccuracyMeters = accuracy,
                currentLocation = newLocation
            )
            return
        }

        lastLocation?.let { previous ->
            val distance = previous.distanceTo(newLocation)
            val elapsedSeconds = ((newLocation.time - previous.time).coerceAtLeast(1L)) / 1000f
            val impliedSpeedMps = distance / elapsedSeconds

            if (distance < MIN_ROUTE_POINT_DISTANCE_METERS || impliedSpeedMps > MAX_WALKING_SPEED_MPS) {
                _workoutState.value = _workoutState.value.copy(gpsAccuracyMeters = accuracy)
                return
            }

            totalDistanceMeters += distance
        }

        lastLocation = newLocation
        pathPointsList.add(newLocation)

        _workoutState.value = _workoutState.value.copy(
            latitude = newLocation.latitude,
            longitude = newLocation.longitude,
            distanceMeters = totalDistanceMeters,
            speedMps = newLocation.speed,
            gpsAccuracyMeters = accuracy,
            currentLocation = newLocation,
            pathPoints = pathPointsList.toList()
        )
        updateNotification()
    }

    private fun buildNotification(): Notification {
        val state = _workoutState.value
        val kmText = String.format("%.2f km", state.distanceMeters / 1000)
        val minutes = state.durationSeconds / 60
        val seconds = state.durationSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val openWorkoutIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_WORKOUT
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            workoutGoalKm?.let { putExtra(EXTRA_GOAL_KM, it) }
            workoutGroupSessionId?.let { putExtra(EXTRA_GROUP_SESSION_ID, it) }
        }
        val openWorkoutPendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            openWorkoutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val boostText = activeModifiers.mapNotNull(::activeModifierNotificationText)
            .joinToString(" | ")
        val notificationText = buildString {
            append("Passos: ${state.steps} | Distância: $kmText")
            if (boostText.isNotBlank()) append(" | $boostText")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitWorkUp - Treino ($timeText)")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openWorkoutPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun activeModifierNotificationText(modifier: ActiveModifier): String? {
        val expiry = runCatching { Instant.parse(modifier.expiresAt) }.getOrNull() ?: return null
        val remaining = Duration.between(Instant.now(), expiry).seconds
        if (remaining <= 0) return null

        val label = when (modifier.effectType.uppercase(Locale.ROOT)) {
            "XP_MULTIPLIER", "XP_BOOST" -> "XP"
            "FITCOINS_MULTIPLIER", "FITCOIN_MULTIPLIER", "COINS_MULTIPLIER" -> "Pontos"
            else -> "Bônus"
        }
        val multiplier = if (modifier.multiplier % 1.0 == 0.0) {
            "x${modifier.multiplier.toInt()}"
        } else {
            "x${String.format(Locale.getDefault(), "%.1f", modifier.multiplier)}"
        }
        val hours = remaining / 3_600
        val minutes = (remaining % 3_600) / 60
        val seconds = remaining % 60
        val time = if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
        return "$label $multiplier ($time)"
    }

    private fun updateNotification() {
        if (!_workoutState.value.isTracking) return
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
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_OPEN_WORKOUT = "com.fitworkup.app.action.OPEN_WORKOUT"
        const val EXTRA_GOAL_KM = "com.fitworkup.app.extra.GOAL_KM"
        const val EXTRA_GROUP_SESSION_ID = "com.fitworkup.app.extra.GROUP_SESSION_ID"
        private const val CHANNEL_ID = "workout_sensor_channel"
        private const val NOTIFICATION_ID = 1001

        private const val LOCATION_INTERVAL_MS = 2000L
        private const val LOCATION_FASTEST_INTERVAL_MS = 1000L
        private const val MAX_ROUTE_ACCURACY_METERS = 15f
        private const val MIN_ROUTE_POINT_DISTANCE_METERS = 3f
        private const val MAX_WALKING_SPEED_MPS = 7f
        private const val RECENT_STEP_WINDOW_MS = 8_000L
    }
}
