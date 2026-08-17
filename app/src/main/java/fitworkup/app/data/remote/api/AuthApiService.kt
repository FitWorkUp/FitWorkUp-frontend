package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.AuthResponseDto
import com.fitworkup.app.data.remote.dto.ForgotPasswordRequestDto
import com.fitworkup.app.data.remote.dto.LoginRequestDto
import com.fitworkup.app.data.remote.dto.MessageResponseDto
import com.fitworkup.app.data.remote.dto.RegisterRequestDto
import com.fitworkup.app.data.remote.dto.ResetPasswordRequestDto
import com.fitworkup.app.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UserResponseDto>

    @POST("api/v1/auth/password/forgot")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): Response<MessageResponseDto>

    @POST("api/v1/auth/password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDto
    ): Response<MessageResponseDto>
}
