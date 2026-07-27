package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.ui.viewmodel.WorkoutUiState

@Composable
fun WorkoutMetricsSection(uiState: WorkoutUiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Bloco de Tempo e Distância
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = uiState.timeFormatted,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp
            )
            Text(
                text = uiState.distanceKmFormatted,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Cards Secundários
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricBox(
                title = "Ritmo (Pace)",
                value = uiState.paceFormatted,
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            MetricBox(
                title = "Ganho Estimado",
                value = "🪙 +${uiState.fitCoinsEarned} Moedas",
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barra de Progresso XP
        Text(
            text = "Progresso de XP da Corrida (+${uiState.xpEarned} XP | ${uiState.steps} passos)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        LinearProgressIndicator(
            progress = { uiState.xpProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun MetricBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}