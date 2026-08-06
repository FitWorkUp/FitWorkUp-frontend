package com.fitworkup.app.domain.repository

import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.DailySummaryResponse

interface ActivityRepository {
    suspend fun registerActivity(request: ActivityRequest): Result<Unit>
    suspend fun getTodaySummary(): Result<DailySummaryResponse>
}