package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
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

@Composable
fun WorkoutMetricsSection(
    uiState: WorkoutUiState,
    modifier: Modifier = Modifier,
    userWeightKg: Float = 70.0f // Peso padrão para cálculo de calorias caso não informado
) {
    // 🧮 1. CÁLCULO DE CALORIAS (MET)
    val met = if (uiState.averageSpeedKmH > 4.0f) 7.0f else 3.5f
    val durationHours = uiState.durationSeconds / 3600.0f
    val caloriesBurned = (met * userWeightKg * durationHours).toInt()

    // 🪙 2. CÁLCULO DE RECOMPENSAS (XP e FitCoins)
    val estimatedXp = (uiState.totalDistanceKm * 100).toInt()
    val estimatedFitCoins = (uiState.totalDistanceKm * 10).toInt()

    // 🛡️ 3. STATUS DINÂMICO DE SINAL DO GPS
    val (gpsStatusText, gpsColor) = when {
        uiState.gpsAccuracyMeters <= 0f -> "Buscando Satélites..." to Color(0xFFFFB300)
        uiState.gpsAccuracyMeters <= 10f -> "Sinal Forte (Verificado)" to Color(0xFF4CAF50)
        uiState.gpsAccuracyMeters <= 25f -> "Sinal Moderado" to Color(0xFFFF9800)
        else -> "Sinal Fraco (Buscando Precision)" to Color(0xFFE53935)
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

        // 📊 GRID 2x2 DE MÉTRICAS PRINCIPAIS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Distância",
                value = String.format(Locale.getDefault(), "%.2f km", uiState.totalDistanceKm),
                icon = Icons.Default.DirectionsRun,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Vel. Média",
                value = String.format(Locale.getDefault(), "%.1f km/h", uiState.averageSpeedKmH),
                icon = Icons.Default.Speed,
                iconColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Calorias",
                value = "$caloriesBurned kcal",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = Color(0xFFFF5722),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Passos Validados",
                value = "${uiState.stepCount}",
                icon = Icons.Default.DirectionsRun,
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
                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp),
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