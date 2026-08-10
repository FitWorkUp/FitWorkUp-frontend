package com.fitworkup.app.data.remote.dto

data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserResponseDto
)
