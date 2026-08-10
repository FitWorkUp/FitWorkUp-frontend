package com.fitworkup.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.repository.ProfileRepository
import com.fitworkup.app.domain.model.BadgeItem
import com.fitworkup.app.domain.model.FriendItem
import com.fitworkup.app.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val friends: List<FriendItem> = emptyList(),
    val badges: List<BadgeItem> = emptyList(),
    val errorMessage: String? = null
)

sealed interface ProfileUiEvent {
    data class ShowSnackbar(val message: String) : ProfileUiEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent: SharedFlow<ProfileUiEvent> = _uiEvent.asSharedFlow()

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            combine(
                profileRepository.getUserProfile(),
                profileRepository.getFriends(),
                profileRepository.getBadges()
            ) { profileResult, friendsResult, badgesResult ->
                val profile = profileResult.getOrNull()
                val friends = friendsResult.getOrDefault(emptyList())
                val badges = badgesResult.getOrDefault(emptyList())

                ProfileUiState(
                    isLoading = false,
                    profile = profile,
                    friends = friends,
                    badges = badges,
                    errorMessage = if (profile == null) "Falha ao carregar dados do perfil." else null
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            val result = profileRepository.removeFriend(friendId)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(friends = state.friends.filter { it.id != friendId })
                }
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Amigo removido com sucesso."))
            } else {
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Erro ao remover amigo."))
            }
        }
    }

    fun sendFriendRequest(userTag: String) {
        viewModelScope.launch {
            val result = profileRepository.sendFriendRequest(userTag)
            if (result.isSuccess) {
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Solicitação enviada para @$userTag!"))
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Erro ao enviar solicitação."
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar(errorMsg))
            }
        }
    }
}
