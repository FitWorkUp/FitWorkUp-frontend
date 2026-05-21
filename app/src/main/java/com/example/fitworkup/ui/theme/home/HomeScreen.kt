package com.example.fitworkup.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.fitworkup.ui.theme.viewmodel.AuthViewModel 
import com.example.fitworkup.ui.theme.home.ContactSection

@Composable
fun HomePage(
    viewModel: AuthViewModel,
    onNavigateToMenu: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Observa o estado do usuário do ViewModel (equivalente ao useUser())
    val user by viewModel.userState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        item {
            HeroSection(
                onGetStartedClick = {
                    if (user != null) {
                        onNavigateToMenu()
                    } else {
                        onNavigateToLogin()
                    }
                }
            )
        }
        item { FeaturesSection() }
        item { StatsSection() }
        item { ContactSection() }
    }
}