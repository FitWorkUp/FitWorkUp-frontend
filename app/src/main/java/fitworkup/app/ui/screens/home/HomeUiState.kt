package com.fitworkup.app.ui.screens.home

import com.fitworkup.app.domain.model.UserActivityItem
import com.google.android.gms.maps.model.LatLng

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Atleta",
    val avatarKey: String = "ICONMAN1",
    val stepsToday: Int = 0,
    val distanceKmToday: Double = 0.0,
    val fitcoins: Int = 0,
    val xp: Int = 0,
    val level: Int = 0,
    val userActivities: List<UserActivityItem> = emptyList(),
    val lastWorkoutPath: List<LatLng> = emptyList(),
    val errorMessage: String? = null
)
