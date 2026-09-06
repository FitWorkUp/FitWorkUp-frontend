package com.fitworkup.app.data.repository

import com.fitworkup.app.data.remote.api.StoreApiService
import com.fitworkup.app.data.remote.api.UserApiService
import com.fitworkup.app.data.remote.dto.InventoryItemDto
import com.fitworkup.app.domain.model.StoreItem
import com.fitworkup.app.domain.model.ActiveModifier
import com.fitworkup.app.domain.model.StorePurchase
import com.fitworkup.app.domain.model.StoreSnapshot
import com.fitworkup.app.domain.repository.StoreRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import retrofit2.Response

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val storeApiService: StoreApiService,
    private val userApiService: UserApiService
) : StoreRepository {

    override suspend fun loadStore(): Result<StoreSnapshot> = runCatching {
        val profile = userApiService.getMyProfile().requireBody()
        val remoteItems = storeApiService.getItems().requireBody()
        val inventoryByStoreItem = storeApiService.getInventory().requireBody()
            .associateBy(InventoryItemDto::storeItemId)
        val activeBoostsByEffect = storeApiService.getActiveBoosts().requireBody()
            .associateBy { it.effectType }

        StoreSnapshot(
            balance = profile.fitcoins,
            items = remoteItems.map { item ->
                val inventoryItem = inventoryByStoreItem[item.id]
                val activeBoost = item.effectType?.let(activeBoostsByEffect::get)
                StoreItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    priceInCoins = item.price,
                    iconEmoji = item.iconEmoji,
                    category = item.category,
                    isPurchased = inventoryItem != null,
                    isEquipped = inventoryItem?.equipped == true,
                    inventoryItemId = inventoryItem?.id,
                    repeatable = item.repeatable,
                    effectType = item.effectType,
                    multiplier = item.multiplier,
                    durationMinutes = item.durationMinutes,
                    activeUntil = activeBoost?.expiresAt
                )
            }
        )
    }

    override suspend fun purchase(storeItemId: Long): Result<StorePurchase> = runCatching {
        val response = storeApiService.purchase(storeItemId).requireBody()
        StorePurchase(
            inventoryItemId = response.inventoryItemId,
            storeItemId = response.storeItemId,
            remainingFitcoins = response.remainingFitcoins,
            message = response.message,
            repeatable = response.repeatable,
            boostExpiresAt = response.boostExpiresAt
        )
    }

    override suspend fun getActiveModifiers(): Result<List<ActiveModifier>> = runCatching {
        storeApiService.getActiveBoosts().requireBody().map { boost ->
            ActiveModifier(
                effectType = boost.effectType,
                multiplier = boost.multiplier,
                expiresAt = boost.expiresAt
            )
        }
    }

    override suspend fun equip(inventoryItemId: Long): Result<Unit> = runCatching {
        storeApiService.equip(inventoryItemId).requireBody()
        Unit
    }

    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) {
            return body() ?: throw IOException("A API retornou uma resposta vazia.")
        }

        val apiMessage = errorBody()?.string()?.let { body ->
            runCatching { JSONObject(body).optString("message") }.getOrNull()
        }
        throw IOException(apiMessage?.takeIf(String::isNotBlank) ?: "Falha na comunicação com a loja.")
    }
}
