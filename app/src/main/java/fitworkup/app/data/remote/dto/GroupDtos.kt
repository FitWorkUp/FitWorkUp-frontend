package com.fitworkup.app.data.remote.dto

data class CreateGroupSessionRequestDto(
    val name: String,
    val targetDistanceKm: Double?,
    val maxParticipants: Int = 5,
    val friendsOnly: Boolean
)

data class JoinGroupSessionRequestDto(
    val code: String
)

data class GroupParticipantDto(
    val id: Long,
    val userId: Long,
    val username: String,
    val ready: Boolean,
    val host: Boolean,
    val currentUser: Boolean
)

data class GroupSessionDto(
    val id: Long,
    val code: String,
    val name: String,
    val targetDistanceKm: Double?,
    val maxParticipants: Int,
    val friendsOnly: Boolean,
    val status: String,
    val createdAt: String,
    val startedAt: String?,
    val currentUserHost: Boolean,
    val currentUserParticipant: Boolean,
    val currentUserReady: Boolean,
    val participants: List<GroupParticipantDto>
)
