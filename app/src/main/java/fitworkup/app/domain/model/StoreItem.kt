package com.fitworkup.app.domain.model

data class StoreItem(
    val id: Long,
    val name: String,
    val description: String = "",
    val priceInCoins: Int,
    val iconEmoji: String,
    val category: String = "AVATAR_BORDER",
    val isPurchased: Boolean = false
)