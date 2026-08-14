package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.FriendshipRequestDto
import com.fitworkup.app.data.remote.dto.FriendshipResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FriendshipApiService {
    @GET("api/v1/friendships")
    suspend fun getFriends(): Response<List<FriendshipResponseDto>>

    @GET("api/v1/friendships/pending")
    suspend fun getPendingRequests(): Response<List<FriendshipResponseDto>>

    @POST("api/v1/friendships/request")
    suspend fun sendRequest(@Body request: FriendshipRequestDto): Response<FriendshipResponseDto>

    @PUT("api/v1/friendships/{id}/accept")
    suspend fun accept(@Path("id") friendshipId: Long): Response<FriendshipResponseDto>

    @PUT("api/v1/friendships/{id}/reject")
    suspend fun reject(@Path("id") friendshipId: Long): Response<Unit>

    @DELETE("api/v1/friendships/{id}")
    suspend fun remove(@Path("id") friendshipId: Long): Response<Unit>
}
