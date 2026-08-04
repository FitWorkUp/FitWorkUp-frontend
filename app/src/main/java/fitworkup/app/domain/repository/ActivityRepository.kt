package com.fitworkup.app.domain.repository

import com.fitworkup.app.data.remote.api.ActivityApiService
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.ActivityResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class UserActivityItem(
    val id: Long,
    val type: String,
    val distanceKm: Double,
    val steps: Int,
    val date: LocalDate = LocalDate.now()
)

@Singleton
class ActivityRepository @Inject constructor(
    private val apiService: ActivityApiService
) {
    private val _activitiesFlow = MutableStateFlow<List<UserActivityItem>>(emptyList())
    val activitiesFlow: StateFlow<List<UserActivityItem>> = _activitiesFlow.asStateFlow()

    data class WorkoutReward(
        val earnedXp: Int,
        val earnedCoins: Int
    )

    fun calculateReward(distanceKm: Double): WorkoutReward {
        val xp = (distanceKm * 10).toInt()
        val coins = (distanceKm * 10).toInt()
        return WorkoutReward(earnedXp = xp, earnedCoins = coins)
    }

    suspend fun registerActivity(request: ActivityRequest): Result<ActivityResponse> {
        return try {
            val response = apiService.registerActivity(request)
            if (response.isSuccessful && response.body() != null) {
                val apiBody = response.body()!!

                val newItem = UserActivityItem(
                    id = apiBody.id,
                    type = apiBody.type,
                    distanceKm = apiBody.distanceKm,
                    steps = apiBody.steps,
                    date = LocalDate.now()
                )

                // Adiciona o novo treino ao histórico em memória
                _activitiesFlow.value = _activitiesFlow.value + newItem

                Result.success(apiBody)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erro no servidor (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha de conexão: ${e.localizedMessage}"))
        }
    }
}