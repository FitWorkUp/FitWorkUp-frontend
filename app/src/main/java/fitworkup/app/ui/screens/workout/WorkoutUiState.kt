package com.fitworkup.app.ui.screens.workout

import android.location.Location

data class WorkoutUiState(
    val isTracking: Boolean = false,
    val totalDistanceKm: Float = 0f,
    val averageSpeedKmH: Float = 0f,
    val totalSteps: Int = 0,
    val acceptedSteps: Int = 0,
    val heldSteps: Int = 0,
    val riskScore: Int = 0,
    val fraudReasons: List<String> = emptyList(),
    val durationSeconds: Long = 0L,
    val gpsAccuracyMeters: Float = 0f,
    val currentLocation: Location? = null,
    val pathPoints: List<Location> = emptyList(),
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean? = null,
    val errorMessage: String? = null
) {
    // Alias para compatibilidade com componentes que consomem stepCount ou distanceKm
    val stepCount: Int get() = totalSteps
    val distanceKm: Double get() = totalDistanceKm.toDouble()
    val avgSpeedKmH: Double get() = averageSpeedKmH.toDouble()
}