package com.fitworkup.app.domain.repository

import com.fitworkup.app.domain.model.RankingUiState

interface RankingRepository {
    suspend fun fetchWeeklyRanking(): Result<RankingUiState.Success>
}
