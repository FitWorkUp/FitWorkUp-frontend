package com.fitworkup.app.ui.screens.workout

import android.location.Location

/**
 * Estado da UI para a tela de treino.
 * Mantém getters de compatibilidade para evitar quebras em telas que consomem nomes legados.
 */
data class WorkoutUiState(
    val isTracking: Boolean = false,
    val totalDistanceKm: Float = 0f,
    val targetDistanceKm: Double? = null,
    val groupSessionId: Long? = null,
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
    // --- ALIASES DE COMPATIBILIDADE ---
    // Evitam a quebra de compilação em componentes e telas secundárias que utilizavam as nomenclaturas anteriores
    val stepCount: Int get() = totalSteps
    val distanceKm: Double get() = totalDistanceKm.toDouble()
    val avgSpeedKmH: Double get() = averageSpeedKmH.toDouble()
    val isSuccess: Boolean get() = submissionSuccess == true
    val hasError: Boolean get() = errorMessage != null
}
