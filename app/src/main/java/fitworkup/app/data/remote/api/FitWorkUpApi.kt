package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FitWorkUpApi {

    @GET("api/v1/activities/today-summary")
    suspend fun getTodaySummary(): Response<DailySummaryResponse>

    @POST("api/v1/activities")
    suspend fun registerActivity(@Body request: ActivityRequest): Response<Unit>
}