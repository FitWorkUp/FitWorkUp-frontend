package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.UserResponseDto
import com.fitworkup.app.data.remote.dto.PublicUserProfileDto
import com.fitworkup.app.data.remote.dto.UserSearchResponseDto
import com.fitworkup.app.domain.model.BadgeItem
import com.fitworkup.app.data.remote.dto.UserAchievementDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @GET("api/v1/users/me")
    suspend fun getMyProfile(): Response<UserResponseDto>

    @GET("api/v1/users/search")
    suspend fun searchUsers(@Query("query") query: String): Response<List<UserSearchResponseDto>>

    @GET("api/v1/users/me/achievements")
    suspend fun getBadges(): Response<List<UserAchievementDto>>

    @GET("api/v1/users/{userId}/public-profile")
    suspend fun getPublicProfile(
        @Path("userId") userId: Long
    ): Response<PublicUserProfileDto>
}
