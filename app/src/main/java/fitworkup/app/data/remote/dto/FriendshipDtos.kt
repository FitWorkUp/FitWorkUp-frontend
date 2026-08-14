package com.fitworkup.app.data.remote.dto

data class FriendshipRequestDto(val friendId: Long)

data class FriendshipResponseDto(
    val id: Long,
    val userId: Long,
    val username: String,
    val friendId: Long,
    val friendUsername: String,
    val status: String,
    val createdAt: String
)

data class UserSearchResponseDto(
    val id: Long,
    val username: String,
    val level: Int,
    val avatarBorder: String,
    val prestigeTitle: String
)
