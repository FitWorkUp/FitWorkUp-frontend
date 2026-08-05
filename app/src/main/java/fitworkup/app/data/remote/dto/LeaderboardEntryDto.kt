package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.RankingUser

data class LeaderboardEntryDto(
    val userId: Long,
    val userName: String,
    val xp: Int,
    val position: Int,
    val avatarUrl: String?
) {
    fun toDomain(currentUserId: Long): RankingUser {
        return RankingUser(
            id = userId.toString(),
            name = userName,
            xp = xp,
            rank = position,
            avatarUrl = avatarUrl,
            isCurrentUser = userId == currentUserId
        )
    }
}