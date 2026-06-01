package com.example.fitworkup.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    // Estado inicial: usuário nulo (não logado)
    private val _userState = MutableStateFlow<String?>(null)
    val userState: StateFlow<String?> = _userState
}