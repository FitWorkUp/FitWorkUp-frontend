package com.fitworkup.app.domain.model
data class BadgeItem(val name: String, val unlocked: Boolean)

data class FriendItem(
    val id: String,
    val name: String,
    val tag: String,
    val level: Int
)