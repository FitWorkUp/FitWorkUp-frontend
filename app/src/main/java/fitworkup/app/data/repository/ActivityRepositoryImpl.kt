package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.FitWorkUpApi
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import com.fitworkup.app.domain.repository.ActivityRepository
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val api: FitWorkUpApi
) : ActivityRepository {

    override suspend fun registerActivity(request: ActivityRequest): Result<Unit> {
        return try {
            val response = api.registerActivity(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro na resposta do servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodaySummary(): Result<DailySummaryResponse> {
        return try {
            val response = api.getTodaySummary()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Falha ao buscar resumo diário: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}