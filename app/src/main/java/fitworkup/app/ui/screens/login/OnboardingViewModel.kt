package com.fitworkup.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.preferences.OnboardingPreferences
import com.fitworkup.app.domain.repository.AuthRepository
import com.fitworkup.app.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _destination = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val destination: SharedFlow<String> = _destination.asSharedFlow()

    private var isCompleting = false

    fun completeOnboarding() {
        if (isCompleting) return
        isCompleting = true

        viewModelScope.launch {
            onboardingPreferences.markCurrentVersionCompleted()
            val destination = if (authRepository.hasSession()) Routes.HOME else Routes.LOGIN
            _destination.emit(destination)
        }
    }
}
