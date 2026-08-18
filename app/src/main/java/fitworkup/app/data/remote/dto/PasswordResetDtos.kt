package com.fitworkup.app.data.remote.dto

data class ForgotPasswordRequestDto(
    val email: String
)

data class ResetPasswordRequestDto(
    val email: String,
    val code: String,
    val newPassword: String
)

data class MessageResponseDto(
    val message: String
)
