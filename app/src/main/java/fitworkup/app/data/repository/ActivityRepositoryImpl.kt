package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.ActivityApiService
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.ActivityResponse
import com.fitworkup.app.domain.model.UserActivityItem
import com.fitworkup.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val apiService: ActivityApiService
) : ActivityRepository {

    private val _activitiesFlow = MutableStateFlow<List<UserActivityItem>>(emptyList())
    override val activitiesFlow: StateFlow<List<UserActivityItem>> = _activitiesFlow.asStateFlow()

    override suspend fun registerActivity(request: ActivityRequest): Result<ActivityResponse> {
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

    override suspend fun fetchActivities(): Result<List<UserActivityItem>> {
        return try {
            Result.success(_activitiesFlow.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}