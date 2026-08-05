package com.fitworkup.app.domain.model

import java.time.LocalDate

data class UserActivityItem(
    val id: Long,
    val type: String,
    val distanceKm: Double,
    val steps: Int,
    val date: LocalDate = LocalDate.now()
)