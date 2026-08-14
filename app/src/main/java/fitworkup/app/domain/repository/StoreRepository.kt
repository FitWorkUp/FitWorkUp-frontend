package com.fitworkup.app.domain.repository

import com.fitworkup.app.domain.model.StorePurchase
import com.fitworkup.app.domain.model.StoreSnapshot

interface StoreRepository {
    suspend fun loadStore(): Result<StoreSnapshot>
    suspend fun purchase(storeItemId: Long): Result<StorePurchase>
    suspend fun equip(inventoryItemId: Long): Result<Unit>
}
