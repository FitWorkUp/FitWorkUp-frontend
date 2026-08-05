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
    val fitCoins: Int
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
    val unlockedAt: String? = null
)