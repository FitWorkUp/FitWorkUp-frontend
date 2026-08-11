package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.RankingApiService
import com.fitworkup.app.domain.model.LeagueInfo
import com.fitworkup.app.domain.model.RankingUiState
import com.fitworkup.app.domain.model.RankingUser
import com.fitworkup.app.domain.repository.RankingRepository
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepositoryImpl @Inject constructor(
    private val rankingApiService: RankingApiService
) : RankingRepository {

    override suspend fun fetchWeeklyRanking(): Result<RankingUiState.Success> {
        return runCatching {
            val response = rankingApiService.getWeeklyRanking()
            val body = response.body()?.takeIf { response.isSuccessful }
                ?: throw IOException("Não foi possível carregar o ranking (${response.code()}).")
            val users = body.entries.map { it.toDomain() }
            val weekStart = LocalDate.parse(body.weekStart)
            val weekEnd = LocalDate.parse(body.weekEnd)
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
            val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), weekEnd).coerceAtLeast(0)

            RankingUiState.Success(
                leagueInfo = LeagueInfo(
                    title = "Ranking Semanal",
                    group = "${weekStart.format(dateFormatter)} a ${weekEnd.format(dateFormatter)}",
                    timeRemaining = when (remainingDays) {
                        0L -> "termina hoje"
                        1L -> "resta 1 dia"
                        else -> "restam $remainingDays dias"
                    }
                ),
                topThree = users.filter { it.rank in 1..3 },
                currentUser = users.find(RankingUser::isCurrentUser),
                otherAthletes = users.filter { it.rank > 3 },
                stepsPerPoint = body.stepsPerPoint
            )
        }
    }
}
