package com.fitworkup.app.domain.model

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracyMeters: Float
)
