package com.fitworkup.app.data.remote.dto

data class DailySummaryResponse(
    val totalSteps: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalCalories: Int = 0,
    val fitcoins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1
)