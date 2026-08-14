package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.AuthResponseDto
import com.fitworkup.app.data.remote.dto.LoginRequestDto
import com.fitworkup.app.data.remote.dto.RegisterRequestDto
import com.fitworkup.app.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UserResponseDto>
}
