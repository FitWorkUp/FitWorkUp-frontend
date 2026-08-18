package com.fitworkup.app.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.repository.ProfileRepository
import com.fitworkup.app.data.connectivity.ConnectivityStatus
import com.fitworkup.app.data.connectivity.NetworkMonitor
import com.fitworkup.app.domain.model.FriendProfileDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendProfileUiState(
    val isLoading: Boolean = true,
    val details: FriendProfileDetails? = null,
    val hasError: Boolean = false
)

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])
    private val _uiState = MutableStateFlow(FriendProfileUiState())
    val uiState: StateFlow<FriendProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (_uiState.value.isLoading && _uiState.value.hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }

            if (networkMonitor.status.first() == ConnectivityStatus.OFFLINE) {
                _uiState.value = FriendProfileUiState(isLoading = false, hasError = true)
                return@launch
            }

            profileRepository.getFriendProfile(userId).fold(
                onSuccess = { details ->
                    _uiState.value = FriendProfileUiState(
                        isLoading = false,
                        details = details
                    )
                },
                onFailure = {
                    _uiState.value = FriendProfileUiState(
                        isLoading = false,
                        hasError = true
                    )
                }
            )
        }
    }
}
