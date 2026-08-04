package com.fitworkup.app.ui.screens.workout

import android.location.Location

data class WorkoutUiState(
    val isTracking: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean? = null,
    val errorMessage: String? = null,
    val totalSteps: Int = 0,
    val acceptedSteps: Int = 0,
    val heldSteps: Int = 0,
    val distanceKm: Double = 0.0,
    val avgSpeedKmH: Double = 0.0,
    val riskScore: Int = 0,
    val fraudReasons: List<String> = emptyList(),
    val currentLocation: Location? = null,
    val pathPoints: List<Location> = emptyList()
)