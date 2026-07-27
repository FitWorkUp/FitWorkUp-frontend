package com.fitworkup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitworkup.app.ui.screens.ranking.RankingTabContent
import com.fitworkup.app.ui.screens.home.components.WorkoutTabContent
import com.fitworkup.app.ui.screens.profile.ProfileScreen
import com.fitworkup.app.ui.screens.store.StorePoints

sealed class HomeTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Workout : HomeTab("workout", "Treino", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun)
    object Ranking : HomeTab("ranking", "Ranking", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Profile : HomeTab("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
    object Store : HomeTab("store", "Loja", Icons.Filled.Storefront, Icons.Outlined.Storefront)
}

@Composable
fun HomeScreen(
    onStartWorkoutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var currentTabRoute by rememberSaveable { mutableStateOf(HomeTab.Workout.route) }
    val tabs = listOf(HomeTab.Workout, HomeTab.Ranking, HomeTab.Store, HomeTab.Profile)
    val currentTab = tabs.find { it.route == currentTabRoute } ?: HomeTab.Workout

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTabRoute = tab.route },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                is HomeTab.Workout -> WorkoutTabContent(onStartWorkout = onStartWorkoutClick)
                is HomeTab.Ranking -> RankingTabContent()
                is HomeTab.Store   -> StorePoints()
                is HomeTab.Profile -> ProfileScreen(onSettingsClick = onSettingsClick)
            }
        }
    }
}