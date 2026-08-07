package com.fitworkup.app.domain.repository

import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.DailySummaryResponse
import com.fitworkup.app.domain.model.UserActivityItem
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    suspend fun registerActivity(request: ActivityRequest): Result<Unit>
    suspend fun getTodaySummary(): Result<DailySummaryResponse>
    fun getLocalActivitiesFlow(): Flow<List<UserActivityItem>>
}