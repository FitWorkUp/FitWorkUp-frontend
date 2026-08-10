package com.fitworkup.app.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val tag: String,
    val title: String,
    val level: Int,
    val currentXp: Int,
    val maxXp: Int,
    val totalKm: Double,
    val streakDays: Int,
    val fitCoins: Int,
    val email: String = "",
    val weightKg: Double? = null,
    val avatarBorder: String = "DEFAULT"
)

data class FriendItem(
    val id: String,
    val name: String,
    val tag: String,
    val level: Int,
    val avatarUrl: String? = null
)

data class BadgeItem(
    val id: String,
    val name: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAt: String? = null,
    val iconName: String? = null,
    val xpReward: Int = 0,
    val fitcoinsReward: Int = 0
)
