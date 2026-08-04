package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.ActivityResponse
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeActivityApiService @Inject constructor() : ActivityApiService {

    override suspend fun registerActivity(request: ActivityRequest): Response<ActivityResponse> {
        // Simula latência de rede de 1 segundo
        delay(1000)

        // Constrói a resposta simulada conforme os atributos exatos do DTO ActivityResponse
        val mockResponse = ActivityResponse(
            id = System.currentTimeMillis(), // Id numérico do tipo Long
            type = request.type,
            distanceKm = request.distanceKm,
            steps = request.steps,
            avgSpeed = request.avgSpeed,
            isValid = (request.riskScore ?: 0) < 5,
            status = if ((request.riskScore ?: 0) < 5) "APPROVED" else "REJECTED",
            acceptedSteps = request.acceptedSteps,
            heldSteps = request.heldSteps,
            riskScore = request.riskScore
        )

        return Response.success(mockResponse)
    }
}