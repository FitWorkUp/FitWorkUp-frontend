package com.fitworkup.app.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.ui.screens.home.components.WorkoutTabContent
import com.fitworkup.app.ui.screens.profile.ProfileScreen
import com.fitworkup.app.ui.screens.ranking.RankingTabRoute
import com.fitworkup.app.ui.screens.store.StorePoints

@Composable
fun HomeScreen(
    onStartWorkoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadTodaySummary()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Treino") },
                    label = { Text("Treino") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Ranking") },
                    label = { Text("Ranking") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Loja") },
                    label = { Text("Loja") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> WorkoutTabContent(
                    homeUiState = homeUiState,
                    onStartWorkout = onStartWorkoutClick
                )
                1 -> RankingTabRoute()
                2 -> StorePoints()
                3 -> ProfileScreen(onSettingsClick = onSettingsClick)
            }
        }
    }
}