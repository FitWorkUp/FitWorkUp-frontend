package com.fitworkup.app.ui.screens.workout

data class WorkoutUiState(
    val isTracking: Boolean = false,
    val totalSteps: Int = 0,
    val acceptedSteps: Int = 0,
    val heldSteps: Int = 0,
    val distanceKm: Double = 0.0,
    val avgSpeedKmH: Double = 0.0,
    val riskScore: Int = 0,
    val fraudReasons: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean? = null,
    val errorMessage: String? = null
)