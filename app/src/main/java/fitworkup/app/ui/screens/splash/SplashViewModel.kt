package com.fitworkup.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.preferences.OnboardingPreferences
import com.fitworkup.app.domain.repository.AuthRepository
import com.fitworkup.app.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {
    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            _destination.value = when {
                onboardingPreferences.needsOnboarding() -> Routes.ONBOARDING
                authRepository.hasSession() -> Routes.HOME
                else -> Routes.LOGIN
            }
        }
    }
}
