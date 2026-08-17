package com.fitworkup.app.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.domain.model.StoreItem
import com.fitworkup.app.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreUiState(
    val isLoading: Boolean = true,
    val balance: Int = 0,
    val items: List<StoreItem> = emptyList(),
    val processingItemId: Long? = null,
    val errorMessage: String? = null,
    val notification: String? = null
)

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            storeRepository.loadStore()
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            balance = snapshot.balance,
                            items = snapshot.items,
                            errorMessage = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Não foi possível carregar a loja."
                        )
                    }
                }
        }
    }

    fun purchase(item: StoreItem) {
        if ((item.isPurchased && !item.repeatable) || _uiState.value.processingItemId != null) return
        if (_uiState.value.balance < item.priceInCoins) {
            _uiState.update { it.copy(notification = "Você não possui FitCoins suficientes.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(processingItemId = item.id) }
            storeRepository.purchase(item.id)
                .onSuccess { purchase ->
                    _uiState.update { state ->
                        state.copy(
                            balance = purchase.remainingFitcoins,
                            processingItemId = null,
                            notification = purchase.message,
                            items = state.items.map { current ->
                                if (current.id == purchase.storeItemId) {
                                    current.copy(
                                        isPurchased = if (purchase.repeatable) current.isPurchased else true,
                                        inventoryItemId = purchase.inventoryItemId ?: current.inventoryItemId,
                                        activeUntil = purchase.boostExpiresAt ?: current.activeUntil
                                    )
                                } else current
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            processingItemId = null,
                            notification = "Não foi possível concluir a compra. Verifique sua conexão."
                        )
                    }
                }
        }
    }

    fun equip(item: StoreItem) {
        val inventoryItemId = item.inventoryItemId ?: return
        if (item.isEquipped || _uiState.value.processingItemId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(processingItemId = item.id) }
            storeRepository.equip(inventoryItemId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            processingItemId = null,
                            notification = "${item.name} equipado no perfil.",
                            items = state.items.map { current ->
                                if (current.category.equals(item.category, ignoreCase = true)) {
                                    current.copy(isEquipped = current.id == item.id)
                                } else current
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            processingItemId = null,
                            notification = "Não foi possível equipar o item. Verifique sua conexão."
                        )
                    }
                }
        }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notification = null) }
    }
}
