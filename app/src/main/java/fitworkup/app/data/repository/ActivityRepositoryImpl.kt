package com.fitworkup.app.data.repository

import android.util.Log
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
            Log.d("ActivityRepository", "Enviando requisição de treino: $request")
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
                Log.d("ActivityRepository", "Treino salvo com sucesso: ${apiBody.id}")
                Result.success(apiBody)
            } else {
                val statusCode = response.code()
                val errorBody = response.errorBody()?.string() ?: "Sem corpo de erro"
                Log.e("ActivityRepository", "Erro HTTP $statusCode: $errorBody")
                Result.failure(Exception("Erro $statusCode no servidor: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Exceção ao registrar atividade", e)
            Result.failure(Exception("Falha na chamada: ${e.localizedMessage ?: e.javaClass.simpleName}"))
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