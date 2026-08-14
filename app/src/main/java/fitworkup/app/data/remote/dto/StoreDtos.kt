package com.fitworkup.app.data.remote.dto

data class StoreItemDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,
    val iconEmoji: String,
    val repeatable: Boolean,
    val effectType: String?,
    val multiplier: Double?,
    val durationMinutes: Int?
)

data class InventoryItemDto(
    val id: Long,
    val storeItemId: Long,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,
    val iconEmoji: String,
    val quantity: Int,
    val equipped: Boolean
)

data class PurchaseResponseDto(
    val inventoryItemId: Long?,
    val storeItemId: Long,
    val quantity: Int?,
    val remainingFitcoins: Int,
    val message: String,
    val repeatable: Boolean,
    val boostExpiresAt: String?
)

data class ActiveBoostDto(
    val effectType: String,
    val multiplier: Double,
    val expiresAt: String
)
