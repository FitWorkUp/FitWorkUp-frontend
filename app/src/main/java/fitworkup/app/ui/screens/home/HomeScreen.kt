package com.fitworkup.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.ui.screens.profile.ProfileScreen
import com.fitworkup.app.ui.screens.store.StorePoints

// ─── Definição das Abas da Bottom Nav ───────────────────────────────────────
sealed class HomeTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
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
fun HomeScreen(
    onStartWorkoutClick: () -> Unit
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                is HomeTab.Workout -> WorkoutTabContent(onStartWorkout = onStartWorkoutClick)
                is HomeTab.Ranking -> RankingTabContent()
                is HomeTab.Store   -> StorePoints()
                is HomeTab.Profile -> ProfileScreen()
            }
        }
    }
}

// ── Conteúdo da Aba de Treino (Com o Gráfico Integrado) ──────────────────

@Preview(showBackground = true)
@Composable
private fun WorkoutTabContent(
    onStartWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. CABEÇALHO: Perfil e Saldo de FitCoins
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Perfil
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil", modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Olá, Atleta!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            // Carteira
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Text("🪙 1.250 FitCoins", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
            }
        }

        // 2. KPI CENTRAL: Contador de Passos (Progresso circular)
        // Aqui você usaria uma biblioteca de gráfico ou um Box com CircularProgressIndicator
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            CircularProgressIndicator(progress = { 0.8f }, modifier = Modifier.fillMaxSize(), strokeWidth = 12.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("8.432", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text("PASSOS / 10.000", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. DUETO DE PERFORMANCE: Distância e Calorias
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PerformanceCard("Distância", "3.2 km", Modifier.weight(1f))
            PerformanceCard("Calorias", "512 kcal", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. BOTÃO DE AÇÃO
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("INICIAR ATIVIDADE", fontWeight = FontWeight.Bold)
        }
    }
}

// Componente auxiliar para os cards do Dueto
@Composable
fun PerformanceCard(title: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}


