package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.UserProfile

data class UserResponseDto(
    val id: Long,
    val username: String,
    val email: String,
    val weightKg: Double?,
    val xp: Int,
    val nextLevelXp: Int,
    val level: Int,
    val fitcoins: Int,
    val streak: Int,
    val totalDistanceKm: Double,
    val avatarBorder: String,
    val avatarKey: String = "ICONMAN1",
    val prestigeTitle: String
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id.toString(),
        name = username,
        tag = username,
        title = prestigeTitle,
        level = level,
        currentXp = xp,
        maxXp = nextLevelXp,
        totalKm = totalDistanceKm,
        streakDays = streak,
        fitCoins = fitcoins,
        email = email,
        weightKg = weightKg,
        avatarBorder = avatarBorder,
        avatarKey = avatarKey
    )
}

data class UpdateAvatarRequestDto(val avatarKey: String)
