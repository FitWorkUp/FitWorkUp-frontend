package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.ActivityResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ActivityApiService {

    @POST("api/activities")
    suspend fun registerActivity(
        @Body request: ActivityRequest
    ): Response<ActivityResponse>
}