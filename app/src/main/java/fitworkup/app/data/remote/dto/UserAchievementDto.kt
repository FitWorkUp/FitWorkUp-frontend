package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.BadgeItem

data class UserAchievementDto(
    val id: Long,
    val name: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAt: String?,
    val iconName: String?,
    val xpReward: Int,
    val fitcoinsReward: Int
) {
    fun toDomain(): BadgeItem = BadgeItem(
        id = id.toString(),
        name = name,
        description = description,
        unlocked = unlocked,
        unlockedAt = unlockedAt,
        iconName = iconName,
        xpReward = xpReward,
        fitcoinsReward = fitcoinsReward
    )
}
