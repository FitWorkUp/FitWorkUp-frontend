package com.fitworkup.app.data.remote.api

import com.fitworkup.app.data.remote.dto.InventoryItemDto
import com.fitworkup.app.data.remote.dto.PurchaseResponseDto
import com.fitworkup.app.data.remote.dto.StoreItemDto
import com.fitworkup.app.data.remote.dto.ActiveBoostDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoreApiService {
    @GET("api/v1/store/items")
    suspend fun getItems(): Response<List<StoreItemDto>>

    @GET("api/v1/store/inventory")
    suspend fun getInventory(): Response<List<InventoryItemDto>>

    @GET("api/v1/store/boosts/active")
    suspend fun getActiveBoosts(): Response<List<ActiveBoostDto>>

    @POST("api/v1/store/purchase/{storeItemId}")
    suspend fun purchase(@Path("storeItemId") storeItemId: Long): Response<PurchaseResponseDto>

    @POST("api/v1/store/equip/{inventoryItemId}")
    suspend fun equip(@Path("inventoryItemId") inventoryItemId: Long): Response<InventoryItemDto>
}
