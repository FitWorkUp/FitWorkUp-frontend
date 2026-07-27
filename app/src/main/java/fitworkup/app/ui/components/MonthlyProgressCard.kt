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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

// Data Model com estado de seleção individual de dia
data class DailyRunProgress(
    val day: Int,
    val distanceKm: Float,
    val isToday: Boolean = false,
    val isFocused: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyProgressCard(
    modifier: Modifier = Modifier
) {
    // 1. Estados Temporais e Seleção
    val today = remember { LocalDate.now() }
    val currentYearMonth = remember { YearMonth.now() }

    var selectedYearMonth by remember { mutableStateOf(currentYearMonth) }
    var focusedDay by remember { mutableStateOf<Int?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Título do Mês Formatado (ex: "Julho 2026")
    val monthTitle = remember(selectedYearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
        selectedYearMonth.format(formatter).replaceFirstChar { it.uppercase() }
    }

    // 2. Dados do Mês
    val monthlyData = remember(selectedYearMonth, focusedDay) {
        getMockDataForYearMonth(selectedYearMonth, today, focusedDay)
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

            // ─── CABEÇALHO COM COMBO: NAVEGAÇÃO + BOTÃO DE CALENDÁRIO ─────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        focusedDay = null
                        selectedYearMonth = selectedYearMonth.minusMonths(1)
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
                }

                // Título e Ícone do Calendário
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
                    onClick = {
                        focusedDay = null
                        selectedYearMonth = selectedYearMonth.plusMonths(1)
                    },
                    enabled = selectedYearMonth < currentYearMonth
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── RESUMO DAS MÉTRICAS ─────────────────────────────────────────
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
                        text = "Atividades",
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

            // ─── GRÁFICO DE BARRAS ROLÁVEL ──────────────────────────────────
            val listState = rememberLazyListState()

            // Efeito para centralizar e rolar para o dia focado ou dia atual
            LaunchedEffect(selectedYearMonth, focusedDay) {
                if (monthlyData.isNotEmpty()) {
                    val targetIndex = when {
                        focusedDay != null -> (focusedDay!! - 1).coerceIn(0, monthlyData.lastIndex)
                        selectedYearMonth == currentYearMonth -> (today.dayOfMonth - 1).coerceIn(0, monthlyData.lastIndex)
                        else -> monthlyData.lastIndex
                    }
                    listState.animateScrollToItem(targetIndex)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
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
                            maxBarHeightDp = 110,
                            onDayClick = { day -> focusedDay = if (focusedDay == day) null else day }
                        )
                    }
                }
            }
        }
    }

    // ─── DIÁLOGO DO CALENDÁRIO MATERIAL 3 ───────────────────────────────────
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

                            // Segurança: Não permite filtrar datas futuras
                            val validDate = if (selectedLocalDate.isAfter(today)) today else selectedLocalDate

                            selectedYearMonth = YearMonth.from(validDate)
                            focusedDay = validDate.dayOfMonth
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

// ─── COMPONENTE DA BARRA INDIVIDUAL ──────────────────────────────────────────
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
        // Balão de KM flutuante
        if (progress.isFocused && progress.distanceKm > 0f) {
            Text(
                text = "${progress.distanceKm} km",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Barra Física
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

        // Rótulo Inferior do Dia
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

// ─── SIMULAÇÃO DE DADOS MOCKADOS COM SUPORTE A FOCO ───────────────────────────
private fun getMockDataForYearMonth(
    yearMonth: YearMonth,
    today: LocalDate,
    focusedDay: Int?
): List<DailyRunProgress> {
    val list = mutableListOf<DailyRunProgress>()
    val totalDaysInMonth = yearMonth.lengthOfMonth()
    val isCurrentMonth = (yearMonth.year == today.year && yearMonth.monthValue == today.monthValue)

    val step = (yearMonth.monthValue % 3) + 2

    for (day in 1..totalDaysInMonth) {
        val isToday = isCurrentMonth && (day == today.dayOfMonth)
        val isFocused = (focusedDay == day)

        val distance = if (isCurrentMonth && day > today.dayOfMonth) {
            0f
        } else if (day % step == 0) {
            (2..7).random() + (0..9).random() / 10f
        } else if (day % 7 == 0) {
            10.2f
        } else {
            0f
        }

        list.add(
            DailyRunProgress(
                day = day,
                distanceKm = distance,
                isToday = isToday,
                isFocused = isFocused
            )
        )
    }
    return list
}