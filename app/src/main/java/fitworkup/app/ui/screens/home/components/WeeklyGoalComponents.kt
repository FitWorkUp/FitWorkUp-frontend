package com.fitworkup.app.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun WeeklyGoalCard(
    enabled: Boolean,
    activeDays: Int,
    targetDays: Int,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeTarget = targetDays.coerceIn(1, 7)
    val completedDays = activeDays.coerceIn(0, 7)
    val progress = if (enabled) {
        (completedDays.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingDays = (safeTarget - completedDays).coerceAtLeast(0)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Meta semanal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (enabled) "Editar" else "Ativar")
                }
            }

            if (enabled) {
                Text(
                    text = "$completedDays de $safeTarget ${if (safeTarget == 1) "dia concluído" else "dias concluídos"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = when {
                        remainingDays == 0 -> "Meta concluída! Continue no seu ritmo."
                        remainingDays == 1 -> "Falta 1 dia para concluir sua meta."
                        else -> "Faltam $remainingDays dias para concluir sua meta."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Ative uma meta para acompanhar sua regularidade durante a semana.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyGoalBottomSheet(
    currentEnabled: Boolean,
    currentTargetDays: Int,
    onSave: (enabled: Boolean, targetDays: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var enabled by remember(currentEnabled) { mutableStateOf(currentEnabled) }
    var targetDays by remember(currentTargetDays) {
        mutableIntStateOf(currentTargetDays.coerceIn(1, 7))
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            Text(
                text = "Configurar meta semanal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Escolha uma meta possível para sua rotina. Você poderá alterá-la quando quiser.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Acompanhar meta", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Exibir o progresso na tela inicial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "$targetDays ${if (targetDays == 1) "dia por semana" else "dias por semana"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = targetDays.toFloat(),
                onValueChange = { targetDays = it.roundToInt().coerceIn(1, 7) },
                valueRange = 1f..7f,
                steps = 5,
                enabled = enabled
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 dia", style = MaterialTheme.typography.labelSmall)
                Text("7 dias", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSave(enabled, targetDays) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("SALVAR META", fontWeight = FontWeight.Bold)
            }
        }
    }
}
