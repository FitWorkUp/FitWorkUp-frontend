package com.fitworkup.app.data.remote.dto

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val weightKg: Double? = null
)
