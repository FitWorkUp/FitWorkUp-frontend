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
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
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

data class WorkoutState(
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedMps: Float = 0f,
    val isTracking: Boolean = false,
    val currentLocation: Location? = null,
    val pathPoints: List<Location> = emptyList()
)

class WorkoutSensorService : Service(), SensorEventListener {

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
        createNotificationChannel()

        pathPointsList.clear()
        totalDistanceMeters = 0f
        lastLocation = null

        _workoutState.value = _workoutState.value.copy(
            isTracking = true,
            distanceMeters = 0f,
            currentLocation = null,
            pathPoints = emptyList()
        )

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

    private fun stopWorkout() {
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
            setMinUpdateDistanceMeters(1.0f) // Atualiza com deslocamentos a partir de 1 metro
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

        // Filtra leituras imprecisas do GPS (imprecisão > 20 metros)
        if (newLocation.hasAccuracy() && newLocation.accuracy > 20f) return

        lastLocation?.let { previous ->
            val distance = previous.distanceTo(newLocation)
            if (distance >= 1.0f) {
                totalDistanceMeters += distance
            }
        }

        lastLocation = newLocation
        pathPointsList.add(newLocation)

        _workoutState.value = _workoutState.value.copy(
            latitude = newLocation.latitude,
            longitude = newLocation.longitude,
            distanceMeters = totalDistanceMeters,
            speedMps = newLocation.speed,
            currentLocation = newLocation,
            pathPoints = pathPointsList.toList()
        )
        updateNotification()
    }

    private fun buildNotification(): Notification {
        val state = _workoutState.value
        val kmText = String.format("%.2f km", state.distanceMeters / 1000)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitWorkUp - Treino em Andamento")
            .setContentText("Passos: ${state.steps} | Distância: $kmText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
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
        private const val CHANNEL_ID = "workout_sensor_channel"
        private const val NOTIFICATION_ID = 1001

        private const val LOCATION_INTERVAL_MS = 2000L // Atualização a cada 2s para maior fidelidade no mapa
        private const val LOCATION_FASTEST_INTERVAL_MS = 1000L
    }
}