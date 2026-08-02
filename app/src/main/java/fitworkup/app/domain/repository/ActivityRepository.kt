package com.fitworkup.app.domain.repository

import com.fitworkup.app.data.remote.api.ActivityApiService
import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.ActivityResponse

class ActivityRepository(
    private val apiService: ActivityApiService
) {

    /**
     * Envia os dados e a telemetria do treino para validação anti-fraude no Backend.
     */
    suspend fun registerActivity(request: ActivityRequest): Result<ActivityResponse> {
        return try {
            val response = apiService.registerActivity(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido no servidor (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Falha de conexão com o servidor: ${e.localizedMessage}"))
        }
    }
}