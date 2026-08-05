package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.UserResponseDto
import com.fitworkup.app.domain.model.BadgeItem
import com.fitworkup.app.domain.model.FriendItem
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): Response<UserResponseDto>

    @GET("users/{id}/friends")
    suspend fun getFriends(@Path("id") userId: String): Response<List<FriendItem>>

    @GET("users/{id}/badges")
    suspend fun getBadges(@Path("id") userId: String): Response<List<BadgeItem>>

    @DELETE("users/{userId}/friends/{friendId}")
    suspend fun removeFriend(
        @Path("userId") userId: String,
        @Path("friendId") friendId: String
    ): Response<Unit>

    @POST("users/friends/request")
    suspend fun sendFriendRequest(@Query("tag") userTag: String): Response<Unit>
}