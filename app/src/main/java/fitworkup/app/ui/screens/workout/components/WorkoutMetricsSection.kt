package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.ui.screens.workout.WorkoutUiState
import java.util.Locale
import kotlin.math.ceil

@Composable
fun WorkoutMetricsSection(
    uiState: WorkoutUiState,
    modifier: Modifier = Modifier
) {
    // Recompensas exibidas apenas como estimativa durante o treino.
    val estimatedXp = (uiState.totalDistanceKm * 100).toInt()
    val estimatedFitCoins = (uiState.totalDistanceKm * 10).toInt()
    val formattedDuration = formatDuration(uiState.durationSeconds)

    // Status dinâmico do sinal do GPS.
    val (gpsStatusText, gpsColor) = when {
        uiState.gpsAccuracyMeters <= 0f -> "Buscando Satélites..." to Color(0xFFFFB300)
        uiState.gpsAccuracyMeters <= 10f -> "Sinal Forte (Verificado)" to Color(0xFF4CAF50)
        uiState.gpsAccuracyMeters <= 25f -> "Sinal Moderado" to Color(0xFFFF9800)
        else -> "Sinal Fraco (Buscando precisão)" to Color(0xFFE53935)
    }

    val animatedGpsColor by animateColorAsState(targetValue = gpsColor, label = "GpsColor")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🟢 BADGE DINÂMICO DE SEGURANÇA E GPS
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = animatedGpsColor.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(animatedGpsColor, shape = RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gpsStatusText,
                    color = animatedGpsColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        uiState.targetDistanceKm?.let { targetKm ->
            DistanceGoalProgress(
                currentDistanceKm = uiState.totalDistanceKm.toDouble(),
                targetDistanceKm = targetKm
            )
        }

        // Grade 2x2 de métricas observáveis, sem estimativa de calorias.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Distância",
                value = String.format(Locale.getDefault(), "%.2f km", uiState.totalDistanceKm),
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Duração",
                value = formattedDuration,
                icon = Icons.Default.Schedule,
                iconColor = Color(0xFF7E57C2),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Vel. Média",
                value = String.format(Locale.getDefault(), "%.1f km/h", uiState.averageSpeedKmH),
                icon = Icons.Default.Speed,
                iconColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Passos Validados",
                value = "${uiState.stepCount}",
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                iconColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        // 🏆 CARD DE RECOMPENSAS GANHAS
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RewardItem(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFC107),
                    label = "XP Estimado",
                    value = "+$estimatedXp XP"
                )
                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                RewardItem(
                    icon = Icons.Default.MonetizationOn,
                    iconColor = Color(0xFFFF9800),
                    label = "FitCoins",
                    value = "+$estimatedFitCoins FC"
                )
            }
        }
    }
}

@Composable
private fun DistanceGoalProgress(
    currentDistanceKm: Double,
    targetDistanceKm: Double
) {
    val safeCurrentKm = currentDistanceKm.coerceAtLeast(0.0)
    val remainingKm = (targetDistanceKm - safeCurrentKm).coerceAtLeast(0.0)
    val progress = (safeCurrentKm / targetDistanceKm).toFloat().coerceIn(0f, 1f)
    val completed = remainingKm <= 0.0
    val remainingLabel = when {
        completed -> "Meta concluída!"
        remainingKm < 1.0 -> "${ceil(remainingKm * 1_000.0).toInt()} m restantes"
        else -> String.format(Locale.getDefault(), "%.2f km restantes", remainingKm)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Meta de distância", fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format(Locale.getDefault(), "%.2f km", targetDistanceKm),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2f km percorridos", safeCurrentKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0L)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun RewardItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
