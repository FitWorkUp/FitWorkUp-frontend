package com.fitworkup.app.data.remote.dto

data class FriendshipRequestDto(val friendId: Long)

data class FriendshipResponseDto(
    val id: Long,
    val userId: Long,
    val username: String,
    val userLevel: Int = 1,
    val userAvatarKey: String = "ICONMAN1",
    val friendId: Long,
    val friendUsername: String,
    val friendLevel: Int = 1,
    val friendAvatarKey: String = "ICONMAN1",
    val status: String,
    val createdAt: String
)

data class UserSearchResponseDto(
    val id: Long,
    val username: String,
    val level: Int,
    val avatarBorder: String,
    val avatarKey: String = "ICONMAN1",
    val prestigeTitle: String
)
