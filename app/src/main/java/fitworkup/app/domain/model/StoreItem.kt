package com.fitworkup.app.domain.model

data class StoreItem(
    val id: Long,
    val name: String,
    val description: String = "",
    val priceInCoins: Int,
    val iconEmoji: String,
    val category: String = "AVATAR_FRAME",
    val isPurchased: Boolean = false,
    val isEquipped: Boolean = false,
    val inventoryItemId: Long? = null,
    val repeatable: Boolean = false,
    val effectType: String? = null,
    val multiplier: Double? = null,
    val durationMinutes: Int? = null,
    val activeUntil: String? = null
)

data class StoreSnapshot(
    val balance: Int,
    val items: List<StoreItem>
)

data class StorePurchase(
    val inventoryItemId: Long?,
    val storeItemId: Long,
    val remainingFitcoins: Int,
    val message: String,
    val repeatable: Boolean,
    val boostExpiresAt: String?
)

data class ActiveModifier(
    val effectType: String,
    val multiplier: Double,
    val expiresAt: String
)
