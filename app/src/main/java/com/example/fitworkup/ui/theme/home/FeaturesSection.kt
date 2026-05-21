package com.example.fitworkup.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FeaturesSection() {
    // Agora o compilador sabe o que é Feature porque criamos a Data Class acima
    val features = listOf(
        Feature("🏋️", "Treinos Personalizados", "Planos adaptados ao seu perfil."),
        Feature("⏱️", "Monitoramento Real", "Acompanhe seu progresso."),
        Feature("🏆", "Gamificação", "Suba no ranking com amigos.")
    )

    val infinite = remember { features + features }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val next = (listState.firstVisibleItemIndex + 1) % infinite.size
            listState.animateScrollToItem(next)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Text(
            text = "Por que usar o FitWorkUp?",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(infinite) { f ->
                // Usando Card do Material 3
                Card(
                    modifier = Modifier.width(280.dp).height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(f.icon, fontSize = 28.sp)
                        Text(f.title, style = MaterialTheme.typography.titleMedium)
                        Text(f.desc, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}