package com.fitworkup.app.domain.repository

import com.fitworkup.app.domain.model.UserProfile

interface AuthRepository {
    suspend fun login(identifier: String, password: String): Result<UserProfile>
    suspend fun register(username: String, email: String, password: String): Result<UserProfile>
    suspend fun logout()
    suspend fun hasSession(): Boolean
}
