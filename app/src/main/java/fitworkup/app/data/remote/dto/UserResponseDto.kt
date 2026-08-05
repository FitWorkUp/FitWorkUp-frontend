package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.UserProfile

data class UserResponseDto(
    val id: Long,
    val name: String,
    val tag: String,
    val title: String?,
    val level: Int,
    val currentXp: Int,
    val maxXp: Int,
    val totalKm: Double,
    val streakDays: Int,
    val fitCoins: Int
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            id = id.toString(),
            name = name,
            tag = tag,
            title = title ?: "Atleta Fit",
            level = level,
            currentXp = currentXp,
            maxXp = maxXp,
            totalKm = totalKm,
            streakDays = streakDays,
            fitCoins = fitCoins
        )
    }
}