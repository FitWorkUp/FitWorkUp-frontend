package com.fitworkup.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun // 👈 IMPORT DO ÍCONE PREENCHIDO
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.DirectionsRun // 👈 IMPORT DO ÍCONE DE CONTORNO
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.ui.screens.profile.ProfileScreen
import androidx.compose.material.icons.outlined.Storefront

// ─── Definição das Abas da Bottom Nav ───────────────────────────────────────
sealed class HomeTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // 👈 ATUALIZADO: Substituindo o PlayArrow pelo DirectionsRun
    object Workout : HomeTab("workout", "Treino", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun)
    object Ranking : HomeTab("ranking", "Ranking", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Profile : HomeTab("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
    object  Store : HomeTab(route = "store", title = "Loja", selectedIcon = Icons.Filled.Storefront, unselectedIcon = Icons.Outlined.Storefront)
}

/**
 * HomeScreen
 *
 * Tela principal que gerencia o estado da Bottom Navigation.
 */
@Composable
fun HomeScreen() {
    var currentTab by remember { mutableStateOf<HomeTab>(HomeTab.Workout) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(HomeTab.Workout, HomeTab.Ranking, HomeTab.Store, HomeTab.Profile)
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Renderiza o conteúdo dinamicamente com base na aba selecionada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                is HomeTab.Workout -> WorkoutTabContent()
                is HomeTab.Ranking -> RankingTabContent()
                is HomeTab.Store   -> StorePoints()
                is HomeTab.Profile -> ProfileScreen()
            }
        }
    }
}

// ── Placeholders para o conteúdo das outras Abas ─────────────────────────

@Composable
private fun WorkoutTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "🏃 Tela de Treino (Iniciar Corrida/GPS)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun StorePoints() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "🛒 Tela loja de pontos ", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun RankingTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "🏆 Tela de Ranking Semanal (Liga Atleta)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}