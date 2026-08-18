package com.fitworkup.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PasswordRecoveryStep {
    EMAIL,
    CODE,
    SUCCESS
}

data class PasswordRecoveryUiState(
    val step: PasswordRecoveryStep = PasswordRecoveryStep.EMAIL,
    val email: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    fun requestCode(email: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Informe um e-mail válido.") }
            return
        }
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, message = null, email = normalizedEmail)
            }
            authRepository.requestPasswordReset(normalizedEmail).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            step = PasswordRecoveryStep.CODE,
                            isLoading = false,
                            message = "Se o e-mail estiver cadastrado, o código chegará em alguns minutos."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
            )
        }
    }

    fun resetPassword(code: String, newPassword: String, passwordConfirmation: String) {
        when {
            !code.matches(Regex("\\d{6}")) -> {
                _uiState.update { it.copy(errorMessage = "Digite os 6 números recebidos por e-mail.") }
                return
            }
            newPassword.length !in 6..72 -> {
                _uiState.update { it.copy(errorMessage = "A nova senha deve ter entre 6 e 72 caracteres.") }
                return
            }
            newPassword != passwordConfirmation -> {
                _uiState.update { it.copy(errorMessage = "As senhas não coincidem.") }
                return
            }
            _uiState.value.isLoading -> return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, message = null) }
            authRepository.resetPassword(_uiState.value.email, code, newPassword).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            step = PasswordRecoveryStep.SUCCESS,
                            isLoading = false,
                            message = "Sua senha foi alterada. Agora você já pode entrar novamente."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
            )
        }
    }

    fun editEmail() {
        _uiState.update {
            it.copy(step = PasswordRecoveryStep.EMAIL, errorMessage = null, message = null)
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
