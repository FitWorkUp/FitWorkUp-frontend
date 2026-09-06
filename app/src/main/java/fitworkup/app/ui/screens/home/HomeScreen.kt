package com.fitworkup.app.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.ui.screens.home.components.WorkoutTabContent
import com.fitworkup.app.ui.screens.home.components.WorkoutSetupAction
import com.fitworkup.app.ui.screens.profile.ProfileScreen
import com.fitworkup.app.ui.screens.ranking.RankingTabRoute
import com.fitworkup.app.ui.screens.store.StorePoints

@Composable
fun HomeScreen(
    onStartWorkoutClick: (WorkoutSetupAction) -> Unit,
    onSettingsClick: () -> Unit,
    onFriendProfileClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadUserProfile()
            viewModel.loadTodaySummary()
            viewModel.loadActiveModifiers()
        }
    }

    // Configuração da cor vermelha para o item selecionado
    val redColor = Color(0xFFE53935) // Tom de vermelho com boa visibilidade
    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,            // Ícone dentro do indicador (Branco)
        selectedTextColor = redColor,               // Texto do item selecionado (Vermelho)
        indicatorColor = redColor,                  // Pílula/Fundo do item selecionado (Vermelho)
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Treino") },
                    label = { Text("Treino") },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Ranking") },
                    label = { Text("Ranking") },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Loja") },
                    label = { Text("Loja") },
                    colors = navBarItemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = navBarItemColors
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> WorkoutTabContent(
                    homeUiState = homeUiState,
                    onStartWorkout = onStartWorkoutClick,
                    onWeeklyGoalChanged = viewModel::updateWeeklyGoal
                )
                1 -> RankingTabRoute()
                2 -> StorePoints()
                3 -> ProfileScreen(
                    onSettingsClick = onSettingsClick,
                    onFriendProfileClick = onFriendProfileClick
                )
            }
        }
    }
}
