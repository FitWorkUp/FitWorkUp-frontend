package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.WeeklyRankingResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface RankingApiService {
    @GET("api/ranking/weekly")
    suspend fun getWeeklyRanking(): Response<WeeklyRankingResponseDto>
}
