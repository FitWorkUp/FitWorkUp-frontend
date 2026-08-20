package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.CreateGroupSessionRequestDto
import com.fitworkup.app.data.remote.dto.GroupSessionDto
import com.fitworkup.app.data.remote.dto.JoinGroupSessionRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GroupApiService {
    @POST("api/v1/groups")
    suspend fun create(@Body request: CreateGroupSessionRequestDto): Response<GroupSessionDto>

    @POST("api/v1/groups/join")
    suspend fun join(@Body request: JoinGroupSessionRequestDto): Response<GroupSessionDto>

    @GET("api/v1/groups/{code}")
    suspend fun get(@Path("code") code: String): Response<GroupSessionDto>

    @PUT("api/v1/groups/{code}/ready/{ready}")
    suspend fun setReady(
        @Path("code") code: String,
        @Path("ready") ready: Boolean
    ): Response<GroupSessionDto>

    @POST("api/v1/groups/{code}/start")
    suspend fun start(@Path("code") code: String): Response<GroupSessionDto>

    @DELETE("api/v1/groups/{code}/participants/me")
    suspend fun leave(@Path("code") code: String): Response<Unit>
}
