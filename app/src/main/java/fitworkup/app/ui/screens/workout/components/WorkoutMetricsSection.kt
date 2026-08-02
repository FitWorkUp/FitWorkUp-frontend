package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fitworkup.app.ui.screens.workout.WorkoutUiState

@Composable
fun WorkoutMetricsSection(
    uiState: WorkoutUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (badgeText, badgeColor) = if (uiState.riskScore >= 2) {
            "Sinal Instável / Em Análise" to Color(0xFFFF9800)
        } else {
            "Sinal Seguro (Verificado)" to Color(0xFF4CAF50)
        }

        Text(
            text = badgeText,
            color = badgeColor,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Validados", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${uiState.acceptedSteps}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF4CAF50)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Em Análise", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${uiState.heldSteps}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFF9800)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Distância: %.2f km".format(uiState.distanceKm))
            Text("Vel. Média: %.1f km/h".format(uiState.avgSpeedKmH))
        }
    }
}