package com.fitworkup.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.ArrayDeque
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WorkoutState(
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val elapsedMillis: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speedMps: Float = 0f,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val locationAvailable: Boolean = false,
    val stepSensorAvailable: Boolean = true,
    val gpsReady: Boolean = false,
    val errorMessage: String? = null
)

class WorkoutSensorService : Service(), SensorEventListener {

    private enum class WorkoutMode(
        val hardMaxSpeedMps: Float,
        val requiresSteps: Boolean
    ) {
        WALKING(3.2f, true),
        RUNNING(7.5f, true),
        CYCLING(18f, false)
    }

    private val binder = LocalBinder()
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> =
        _workoutState.asStateFlow()

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var usesStepCounter = false
    private var sensorsRegistered = false
    private var lastRawStepCount: Int? = null
    private var accumulatedSteps = 0
    private var lastStepAtElapsedMillis = 0L

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationUpdatesRegistered = false

    private var workoutMode = WorkoutMode.WALKING
    private var lastAcceptedLocation: Location? = null
    private var totalDistanceMeters = 0f
    private var gpsWarmupFixes = 0
    private var rejectedJumpCount = 0
    private val recentSpeeds = ArrayDeque<Float>()

    private var accumulatedElapsedMillis = 0L
    private var activePeriodStartedAt: Long? = null
    private var timerJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutSensorService =
            this@WorkoutSensorService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()

        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        stepSensor = sensorManager.getDefaultSensor(
            Sensor.TYPE_STEP_COUNTER
        )
        usesStepCounter = stepSensor != null

        if (stepSensor == null) {
            stepSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_STEP_DETECTOR
            )
        }

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!_workoutState.value.isTracking ||
                    _workoutState.value.isPaused
                ) {
                    return
                }

                result.locations
                    .sortedBy { it.elapsedRealtimeNanos }
                    .forEach(::processLocation)
            }
        }

        _workoutState.update {
            it.copy(stepSensorAvailable = stepSensor != null)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START -> startNewWorkout(
                intent.getStringExtra(EXTRA_WORKOUT_TYPE)
            )
            ACTION_PAUSE -> pauseWorkout()
            ACTION_RESUME -> resumeWorkout()
            ACTION_STOP -> stopWorkout()
        }

        return START_NOT_STICKY
    }

    private fun startNewWorkout(type: String?) {
        if (_workoutState.value.isTracking) return

        if (!hasRequiredPermissions()) {
            _workoutState.value = WorkoutState(
                stepSensorAvailable = stepSensor != null,
                errorMessage =
                    "Permissões de localização e atividade física não concedidas."
            )
            stopSelf()
            return
        }

        workoutMode = when (type?.uppercase(Locale.ROOT)) {
            "CORRIDA", "RUNNING" -> WorkoutMode.RUNNING
            "CICLISMO", "CYCLING" -> WorkoutMode.CYCLING
            else -> WorkoutMode.WALKING
        }

        resetSession()
        createNotificationChannel()
        startAsForegroundService()

        _workoutState.update {
            it.copy(
                isTracking = true,
                isPaused = false,
                errorMessage = null
            )
        }

        startActivePeriod()
        registerStepSensor()
        startLocationUpdates()
        startTimerTicker()
        updateNotification()
    }

    private fun pauseWorkout() {
        val state = _workoutState.value
        if (!state.isTracking || state.isPaused) return

        finishActivePeriod()
        unregisterStepSensor()
        stopLocationUpdates()

        lastRawStepCount = null
        resetGpsAnchor()

        _workoutState.update {
            it.copy(
                elapsedMillis = accumulatedElapsedMillis,
                speedMps = 0f,
                isPaused = true,
                gpsReady = false
            )
        }
        updateNotification()
    }

    private fun resumeWorkout() {
        val state = _workoutState.value
        if (!state.isTracking || !state.isPaused) return

        lastRawStepCount = null
        resetGpsAnchor()

        _workoutState.update {
            it.copy(isPaused = false, speedMps = 0f)
        }

        startActivePeriod()
        registerStepSensor()
        startLocationUpdates()
        updateNotification()
    }

    private fun stopWorkout() {
        if (_workoutState.value.isTracking &&
            !_workoutState.value.isPaused
        ) {
            finishActivePeriod()
        }

        unregisterStepSensor()
        stopLocationUpdates()
        stopTimerTicker()

        _workoutState.update {
            it.copy(
                elapsedMillis = accumulatedElapsedMillis,
                speedMps = 0f,
                isTracking = false,
                isPaused = false
            )
        }

        ServiceCompat.stopForeground(
            this,
            ServiceCompat.STOP_FOREGROUND_REMOVE
        )
        stopSelf()
    }

    private fun resetSession() {
        unregisterStepSensor()
        stopLocationUpdates()
        stopTimerTicker()

        lastRawStepCount = null
        accumulatedSteps = 0
        lastStepAtElapsedMillis = 0L
        totalDistanceMeters = 0f
        resetGpsAnchor()

        accumulatedElapsedMillis = 0L
        activePeriodStartedAt = null

        _workoutState.value = WorkoutState(
            stepSensorAvailable = stepSensor != null
        )
    }

    private fun resetGpsAnchor() {
        lastAcceptedLocation = null
        gpsWarmupFixes = 0
        rejectedJumpCount = 0
        recentSpeeds.clear()
    }

    private fun startActivePeriod() {
        if (activePeriodStartedAt == null) {
            activePeriodStartedAt = SystemClock.elapsedRealtime()
        }
    }

    private fun finishActivePeriod() {
        val startedAt = activePeriodStartedAt ?: return
        accumulatedElapsedMillis +=
            SystemClock.elapsedRealtime() - startedAt
        activePeriodStartedAt = null
    }

    private fun currentElapsedMillis(): Long {
        val startedAt = activePeriodStartedAt
        return if (startedAt == null) {
            accumulatedElapsedMillis
        } else {
            accumulatedElapsedMillis +
                    (SystemClock.elapsedRealtime() - startedAt)
        }
    }

    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                if (_workoutState.value.isTracking &&
                    !_workoutState.value.isPaused
                ) {
                    _workoutState.update {
                        it.copy(elapsedMillis = currentElapsedMillis())
                    }
                }
                delay(TIMER_INTERVAL_MS)
            }
        }
    }

    private fun stopTimerTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun registerStepSensor() {
        if (sensorsRegistered) return
        val sensor = stepSensor ?: return

        sensorsRegistered = sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun unregisterStepSensor() {
        if (!sensorsRegistered) return
        sensorManager.unregisterListener(this)
        sensorsRegistered = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorEvent = event ?: return
        val state = _workoutState.value
        if (!state.isTracking || state.isPaused) return

        when (sensorEvent.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val raw = sensorEvent.values.firstOrNull()?.toInt()
                    ?: return
                val previous = lastRawStepCount

                if (previous == null || raw < previous) {
                    lastRawStepCount = raw
                    return
                }

                val delta = raw - previous
                lastRawStepCount = raw

                if (delta in 1..MAX_REASONABLE_STEP_BATCH) {
                    addSteps(delta)
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if ((sensorEvent.values.firstOrNull() ?: 0f) > 0f) {
                    addSteps(1)
                }
            }
        }
    }

    private fun addSteps(delta: Int) {
        accumulatedSteps += delta
        lastStepAtElapsedMillis = SystemClock.elapsedRealtime()
        _workoutState.update { it.copy(steps = accumulatedSteps) }
        updateNotification()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationUpdatesRegistered || !hasLocationPermission()) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MS)
            setMinUpdateDistanceMeters(LOCATION_MIN_DISTANCE_METERS)
            setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MS)
            setWaitForAccurateLocation(true)
        }.build()

        locationUpdatesRegistered = true
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { error ->
            locationUpdatesRegistered = false
            _workoutState.update {
                it.copy(
                    errorMessage =
                        "Não foi possível iniciar o GPS: ${error.localizedMessage}"
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        if (!::fusedLocationClient.isInitialized ||
            !::locationCallback.isInitialized
        ) return

        locationUpdatesRegistered = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun processLocation(location: Location) {
        if (!isLocationUsable(location)) return

        if (gpsWarmupFixes < REQUIRED_WARMUP_FIXES) {
            gpsWarmupFixes++
            publishLocation(location, 0f, false)

            if (gpsWarmupFixes == REQUIRED_WARMUP_FIXES) {
                lastAcceptedLocation = location
                _workoutState.update { it.copy(gpsReady = true) }
            }
            return
        }

        val previous = lastAcceptedLocation ?: run {
            lastAcceptedLocation = location
            return
        }

        val elapsedSeconds =
            (location.elapsedRealtimeNanos -
                    previous.elapsedRealtimeNanos) / 1_000_000_000f

        if (elapsedSeconds < MIN_SEGMENT_SECONDS) return

        val distance = previous.distanceTo(location)
        if (distance < MIN_SEGMENT_METERS) {
            publishLocation(location, 0f, true)
            return
        }

        val speed = distance / elapsedSeconds
        val accuracyNoise = max(previous.accuracy, location.accuracy)
            .times(ACCURACY_NOISE_FACTOR)
            .coerceIn(MIN_SEGMENT_METERS, MAX_NOISE_METERS)

        val hasRecentSteps =
            SystemClock.elapsedRealtime() - lastStepAtElapsedMillis <=
                    RECENT_STEP_WINDOW_MS

        val impossibleForMode = speed > workoutMode.hardMaxSpeedMps
        val suspiciousWithoutSteps =
            workoutMode.requiresSteps &&
                    stepSensor != null &&
                    !hasRecentSteps &&
                    distance > MAX_DISTANCE_WITHOUT_RECENT_STEPS_METERS

        if (impossibleForMode || suspiciousWithoutSteps) {
            rejectedJumpCount++

            if (rejectedJumpCount >= JUMPS_BEFORE_REANCHOR) {
                lastAcceptedLocation = location
                rejectedJumpCount = 0
                recentSpeeds.clear()
            }
            return
        }

        rejectedJumpCount = 0

        if (distance < accuracyNoise) {
            publishLocation(location, 0f, true)
            return
        }

        totalDistanceMeters += distance
        lastAcceptedLocation = location

        recentSpeeds.addLast(speed)
        while (recentSpeeds.size > SPEED_WINDOW_SIZE) {
            recentSpeeds.removeFirst()
        }

        publishLocation(location, medianSpeed(), true)
        updateNotification()
    }

    private fun medianSpeed(): Float {
        if (recentSpeeds.isEmpty()) return 0f
        val values = recentSpeeds.toList().sorted()
        return values[values.size / 2]
    }

    private fun isLocationUsable(location: Location): Boolean {
        if (!location.hasAccuracy() ||
            location.accuracy <= 0f ||
            location.accuracy > MAX_ACCURACY_METERS
        ) return false

        if (location.latitude !in -90.0..90.0 ||
            location.longitude !in -180.0..180.0
        ) return false

        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
        if (isMock) return false

        val ageMillis =
            (SystemClock.elapsedRealtimeNanos() -
                    location.elapsedRealtimeNanos) / 1_000_000L

        return ageMillis in 0..MAX_LOCATION_AGE_MS
    }

    private fun publishLocation(
        location: Location,
        filteredSpeedMps: Float,
        gpsReady: Boolean
    ) {
        _workoutState.update {
            it.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                distanceMeters = totalDistanceMeters,
                speedMps = filteredSpeedMps,
                locationAvailable = true,
                gpsReady = gpsReady
            )
        }
    }

    private fun hasRequiredPermissions(): Boolean =
        hasLocationPermission() && hasActivityRecognitionPermission()

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasActivityRecognitionPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED

    private fun startAsForegroundService() {
        val type = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            else -> 0
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            type
        )
    }

    private fun buildNotification(): Notification {
        val state = _workoutState.value
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(
                if (state.isPaused) "FitWorkUp — treino pausado"
                else "FitWorkUp — treino em andamento"
            )
            .setContentText(
                String.format(
                    Locale.getDefault(),
                    "Passos: %d | Distância: %.2f km",
                    state.steps,
                    state.distanceMeters / 1_000f
                )
            )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(state.isTracking)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        if (!_workoutState.value.isTracking) return
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitoramento de atividade física",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        unregisterStepSensor()
        stopLocationUpdates()
        stopTimerTicker()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START =
            "com.fitworkup.app.action.START_WORKOUT"
        const val ACTION_PAUSE =
            "com.fitworkup.app.action.PAUSE_WORKOUT"
        const val ACTION_RESUME =
            "com.fitworkup.app.action.RESUME_WORKOUT"
        const val ACTION_STOP =
            "com.fitworkup.app.action.STOP_WORKOUT"
        const val EXTRA_WORKOUT_TYPE = "workout_type"

        private const val CHANNEL_ID = "workout_sensor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TIMER_INTERVAL_MS = 1_000L

        private const val MAX_REASONABLE_STEP_BATCH = 500
        private const val RECENT_STEP_WINDOW_MS = 8_000L

        private const val LOCATION_INTERVAL_MS = 2_000L
        private const val LOCATION_MIN_INTERVAL_MS = 1_000L
        private const val LOCATION_MIN_DISTANCE_METERS = 1.5f
        private const val MAX_LOCATION_AGE_MS = 5_000L
        private const val MAX_ACCURACY_METERS = 25f
        private const val REQUIRED_WARMUP_FIXES = 3

        private const val MIN_SEGMENT_SECONDS = 0.8f
        private const val MIN_SEGMENT_METERS = 1.5f
        private const val ACCURACY_NOISE_FACTOR = 0.45f
        private const val MAX_NOISE_METERS = 7f
        private const val MAX_DISTANCE_WITHOUT_RECENT_STEPS_METERS = 12f
        private const val JUMPS_BEFORE_REANCHOR = 3
        private const val SPEED_WINDOW_SIZE = 5
    }
}
