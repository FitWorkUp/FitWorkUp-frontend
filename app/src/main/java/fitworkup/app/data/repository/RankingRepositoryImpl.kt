package com.fitworkup.app.data.repository

import com.fitworkup.app.domain.model.LeagueInfo
import com.fitworkup.app.domain.model.RankingUiState
import com.fitworkup.app.domain.model.RankingUser
import com.fitworkup.app.domain.repository.RankingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepositoryImpl @Inject constructor(
    // Injete aqui seu Retrofit ApiService quando integrado com o backend
) : RankingRepository {

    override suspend fun fetchWeeklyRanking(): Result<RankingUiState.Success> {
        return try {
            // Simulação de resposta remota (Substituir por chamada Retrofit real)
            val league = LeagueInfo(
                title = "Liga Ouro",
                group = "Grupo 4",
                timeRemaining = "3 dias e 04h"
            )

            val mockUsers = listOf(
                RankingUser("1", "Ana Silva", 3120, 1),
                RankingUser("2", "Carlos M.", 2450, 2),
                RankingUser("3", "Pedro R.", 2100, 3),
                RankingUser("4", "Julia Lima", 1890, 4),
                RankingUser("5", "Marcos V.", 1640, 5),
                RankingUser("6", "Lucas Dias", 1450, 6),
                RankingUser("7", "Ronaldo S.", 1250, 7, isCurrentUser = true)
            )

            val topThree = mockUsers.filter { it.rank in 1..3 }
            val currentUser = mockUsers.find { it.isCurrentUser }
            val others = mockUsers.filter { it.rank > 3 }

            Result.success(
                RankingUiState.Success(
                    leagueInfo = league,
                    topThree = topThree,
                    currentUser = currentUser,
                    otherAthletes = others
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
