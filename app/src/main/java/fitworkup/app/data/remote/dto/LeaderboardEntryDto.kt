package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.RankingUser

data class LeaderboardEntryDto(
    val userId: Long,
    val username: String,
    val position: Int,
    val validatedSteps: Long,
    val movementPoints: Long,
    val activeDays: Int,
    val currentUser: Boolean
) {
    fun toDomain(): RankingUser {
        return RankingUser(
            id = userId.toString(),
            name = username,
            movementPoints = movementPoints,
            validatedSteps = validatedSteps,
            activeDays = activeDays,
            rank = position,
            isCurrentUser = currentUser
        )
    }
}

data class WeeklyRankingResponseDto(
    val weekStart: String,
    val weekEnd: String,
    val stepsPerPoint: Int,
    val entries: List<LeaderboardEntryDto>
)
