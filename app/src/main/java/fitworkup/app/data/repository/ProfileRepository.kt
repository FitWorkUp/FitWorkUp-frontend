package com.fitworkup.app.data.repository

import com.fitworkup.app.domain.model.BadgeItem
import com.fitworkup.app.domain.model.FriendItem
import com.fitworkup.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<Result<UserProfile>>
    fun getFriends(): Flow<Result<List<FriendItem>>>
    fun getBadges(): Flow<Result<List<BadgeItem>>>

    suspend fun removeFriend(friendId: String): Result<Unit>
    suspend fun sendFriendRequest(userTagOrEmail: String): Result<Unit>
    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit>
    suspend fun rejectFriendRequest(friendshipId: String): Result<Unit>
}
