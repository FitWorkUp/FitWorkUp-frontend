package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitworkup.app.ui.screens.home.HomeUiState
import java.util.Locale

@Composable
fun WorkoutTabContent(
    homeUiState: HomeUiState = HomeUiState(),
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Resumo de Hoje", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Passos: ${homeUiState.stepsToday}")
                Text(text = String.format(Locale.getDefault(), "Distância: %.2f km", homeUiState.distanceKmToday))
                Text(text = "Calorias: ${homeUiState.caloriesToday} kcal")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Iniciar Treino")
        }
    }
}       