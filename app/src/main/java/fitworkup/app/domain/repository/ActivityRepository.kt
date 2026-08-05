package com.fitworkup.app.domain.repository

import com.fitworkup.app.data.remote.dto.ActivityRequest
import com.fitworkup.app.data.remote.dto.ActivityResponse
import com.fitworkup.app.domain.model.UserActivityItem
import kotlinx.coroutines.flow.StateFlow

interface ActivityRepository {
    val activitiesFlow: StateFlow<List<UserActivityItem>>
    suspend fun registerActivity(request: ActivityRequest): Result<ActivityResponse>
    suspend fun fetchActivities(): Result<List<UserActivityItem>>
}