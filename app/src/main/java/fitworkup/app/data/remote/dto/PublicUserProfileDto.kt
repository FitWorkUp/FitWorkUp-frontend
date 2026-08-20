package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.FriendProfileDetails
import com.fitworkup.app.domain.model.UserProfile

data class PublicUserProfileDto(
    val id: Long,
    val username: String,
    val xp: Int,
    val nextLevelXp: Int,
    val level: Int,
    val streak: Int,
    val totalDistanceKm: Double,
    val avatarBorder: String,
    val avatarKey: String = "ICONMAN1",
    val prestigeTitle: String,
    val achievements: List<UserAchievementDto>
) {
    fun toDomain(): FriendProfileDetails = FriendProfileDetails(
        profile = UserProfile(
            id = id.toString(),
            name = username,
            tag = username,
            title = prestigeTitle,
            level = level,
            currentXp = xp,
            maxXp = nextLevelXp,
            totalKm = totalDistanceKm,
            streakDays = streak,
            fitCoins = 0,
            avatarBorder = avatarBorder,
            avatarKey = avatarKey
        ),
        badges = achievements.map(UserAchievementDto::toDomain)
    )
}
