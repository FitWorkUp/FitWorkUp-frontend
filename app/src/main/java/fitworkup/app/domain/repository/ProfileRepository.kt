package com.fitworkup.app.domain.repository

import com.fitworkup.app.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val userProfileFlow: StateFlow<UserProfile?>
    suspend fun fetchProfile(): Result<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Result<Unit>
}