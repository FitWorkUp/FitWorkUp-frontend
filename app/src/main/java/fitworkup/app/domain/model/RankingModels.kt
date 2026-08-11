package com.fitworkup.app.domain.model

data class RankingUser(
    val id: String,
    val name: String,
    val movementPoints: Long,
    val validatedSteps: Long,
    val activeDays: Int,
    val rank: Int,
    val avatarUrl: String? = null,
    val isCurrentUser: Boolean = false
)

data class LeagueInfo(
    val title: String,
    val group: String,
    val timeRemaining: String
)

sealed interface RankingUiState {
    object Loading : RankingUiState
    data class Success(
        val leagueInfo: LeagueInfo,
        val topThree: List<RankingUser>,
        val currentUser: RankingUser?,
        val otherAthletes: List<RankingUser>,
        val stepsPerPoint: Int
    ) : RankingUiState
    data class Error(val message: String) : RankingUiState
}
