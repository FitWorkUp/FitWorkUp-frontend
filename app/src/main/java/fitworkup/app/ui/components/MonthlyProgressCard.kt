package com.fitworkup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.domain.model.UserActivityItem
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DailyRunProgress(
    val day: Int,
    val distanceKm: Float,
    val isToday: Boolean = false,
    val isFocused: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyProgressCard(
    monthlyData: List<DailyRunProgress>,
    selectedYearMonth: YearMonth,
    focusedDay: Int?,
    dayActivities: List<UserActivityItem> = emptyList(),
    dayTotalKm: Double = 0.0,
    onMonthChanged: (YearMonth) -> Unit,
    onDayFocused: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val currentYearMonth = remember { YearMonth.now() }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val monthTitle = remember(selectedYearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
        selectedYearMonth.format(formatter).replaceFirstChar { it.uppercase() }
    }

    val totalKm = monthlyData.sumOf { it.distanceKm.toDouble() }.toFloat()
    val totalRuns = monthlyData.count { it.distanceKm > 0f }
    val maxDistance = monthlyData.maxOfOrNull { it.distanceKm }?.takeIf { it > 0f } ?: 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // CABEÇALHO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(selectedYearMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = monthTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Abrir Calendário",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onMonthChanged(selectedYearMonth.plusMonths(1)) },
                    enabled = selectedYearMonth < currentYearMonth
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RESUMO DAS MÉTRICAS MENSAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Distância Total",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f km", totalKm),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Dias com Treino",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$totalRuns",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GRÁFICO DE BARRAS ROLÁVEL
            val listState = rememberLazyListState()

            LaunchedEffect(selectedYearMonth, focusedDay, monthlyData) {
                if (monthlyData.isNotEmpty()) {
                    val targetIndex = when {
                        focusedDay != null -> (focusedDay - 1).coerceIn(0, monthlyData.lastIndex)
                        selectedYearMonth == currentYearMonth -> (today.dayOfMonth - 1).coerceIn(0, monthlyData.lastIndex)
                        else -> monthlyData.lastIndex
                    }
                    listState.animateScrollToItem(targetIndex)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(monthlyData) { progress ->
                        BarItem(
                            progress = progress,
                            maxDistance = maxDistance,
                            maxBarHeightDp = 90,
                            onDayClick = { day -> onDayFocused(day) }
                        )
                    }
                }
            }

            // DETALHAMENTO DO DIA SELECIONADO
            if (focusedDay != null) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Atividades do dia $focusedDay de ${selectedYearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = String.format(Locale.getDefault(), "Total no dia: %.2f km", dayTotalKm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (dayActivities.isEmpty()) {
                    Text(
                        text = "Nenhuma atividade registrada neste dia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        for ((index, activity) in dayActivities.withIndex()) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(
                                                text = "Atividade #${index + 1} - ${activity.type}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${activity.steps} passos",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2f km", activity.distanceKm),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val initialEpochMillis = selectedYearMonth.atDay(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialEpochMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            val validDate = if (selectedLocalDate.isAfter(today)) today else selectedLocalDate
                            onMonthChanged(YearMonth.from(validDate))
                            onDayFocused(validDate.dayOfMonth)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Selecione o Dia ou Mês",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}

@Composable
private fun BarItem(
    progress: DailyRunProgress,
    maxDistance: Float,
    maxBarHeightDp: Int,
    onDayClick: (Int) -> Unit
) {
    val barHeight = if (maxDistance > 0) {
        (progress.distanceKm / maxDistance) * maxBarHeightDp
    } else {
        0f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(32.dp)
            .clickable { onDayClick(progress.day) }
    ) {
        if (progress.isFocused && progress.distanceKm > 0f) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f", progress.distanceKm),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        Box(
            modifier = Modifier
                .width(12.dp)
                .height(maxOf(barHeight.dp, 6.dp))
                .clip(CircleShape)
                .background(
                    when {
                        progress.isFocused -> MaterialTheme.colorScheme.secondary
                        progress.distanceKm > 0f -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    }
                )
                .then(
                    if (progress.isToday) {
                        Modifier.border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CircleShape
                        )
                    } else Modifier
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = progress.day.toString(),
            fontSize = 11.sp,
            fontWeight = if (progress.isToday || progress.isFocused) FontWeight.ExtraBold else if (progress.distanceKm > 0f) FontWeight.Bold else FontWeight.Normal,
            color = when {
                progress.isFocused -> MaterialTheme.colorScheme.secondary
                progress.isToday -> MaterialTheme.colorScheme.tertiary
                progress.distanceKm > 0f -> MaterialTheme.colorScheme.onBackground
                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            },
            textAlign = TextAlign.Center
        )
    }
}