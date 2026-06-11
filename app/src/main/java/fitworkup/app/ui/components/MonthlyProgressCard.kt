package com.fitworkup.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import java.util.Calendar

@Composable
fun MonthlyProgressCard() {
    // 1. Estado do Filtro (Mês Atual)
    // Em um cenário real, você usaria YearMonth ou o repositório/ViewModel.
    // Vamos usar um índice simples de 0 a 3 para simular a troca de meses.
    val months = listOf("Março 2026", "Abril 2026", "Maio 2026", "Junho 2026")
    var selectedMonthIndex by remember { mutableStateOf(3) } // Começa em Junho 2026

    // 2. Mock de dados filtrados por mês
    val monthlyData = remember(selectedMonthIndex) {
        getMockDataForMonth(months[selectedMonthIndex])
    }

    // Cálculos de resumo do mês selecionado
    val totalKm = monthlyData.sumOf { it.distanceKm.toDouble() }.toFloat()
    val totalRuns = monthlyData.count { it.distanceKm > 0f }
    val maxDistance = monthlyData.maxOfOrNull { it.distanceKm } ?: 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ─── SELETOR / FILTRO MENSAL ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (selectedMonthIndex > 0) selectedMonthIndex-- },
                    enabled = selectedMonthIndex > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
                }

                Text(
                    text = months[selectedMonthIndex],
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { if (selectedMonthIndex < months.lastIndex) selectedMonthIndex++ },
                    enabled = selectedMonthIndex < months.lastIndex
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── RESUMO DE MÉTRICAS DO MÊS FILTRADO ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Distância Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text(text = String.format("%.1f km", totalKm), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Atividades", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text(text = "$totalRuns", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── O GRÁFICO DE BARRAS ROLÁVEL ───────────────────────────────────
            val listState = rememberLazyListState()

            // Força o gráfico a rolar automaticamente para o final ao mudar de mês
            LaunchedEffect(monthlyData) {
                if (monthlyData.isNotEmpty()) {
                    listState.animateScrollToItem(monthlyData.lastIndex)
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
                            maxBarHeightDp = 120 // Altura máxima que a barra pode atingir
                        )
                    }
                }
            }
        }
    }
}

// ─── COMPONENTE DA BARRA INDIVIDUAL ──────────────────────────────────────────
@Composable
private fun BarItem(
    progress: DailyRunProgress,
    maxDistance: Float,
    maxBarHeightDp: Int
) {
    // Estado para animação de clique na barra (mostra um balão com o KM se clicado)
    var isSelected by remember { mutableStateOf(false) }

    // Calcula a altura proporcional da barra com base na maior corrida do mês
    val barHeight = if (maxDistance > 0) {
        (progress.distanceKm / maxDistance) * maxBarHeightDp
    } else {
        0f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(32.dp)
            .clickable { isSelected = !isSelected }
    ) {
        // Balão de KM flutuante (Aparece se o usuário clicar na barra)
        if (isSelected && progress.distanceKm > 0f) {
            Text(
                text = "${progress.distanceKm}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp)) // Reserva o espaço vertical
        }

        // A Barra Física
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(maxOf(barHeight.dp, 4.dp)) // Garante um tamanho mínimo para dias zerados
                .clip(CircleShape)
                .background(
                    if (progress.distanceKm > 0f) {
                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f) // Dia sem treino
                    }
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Dia do Mês (Legenda inferior)
        Text(
            text = progress.day.toString(),
            fontSize = 11.sp,
            fontWeight = if (progress.distanceKm > 0f) FontWeight.Bold else FontWeight.Normal,
            color = if (progress.distanceKm > 0f) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

// ─── GERADOR DE DADOS DE TESTE (MOCK) ────────────────────────────────────────
private fun getMockDataForMonth(monthName: String): List<DailyRunProgress> {
    val list = mutableListOf<DailyRunProgress>()
    val days = if (monthName.contains("Abril")) 30 else 31

    // Semente diferente dependendo do mês para simular a mudança no gráfico
    val step = when {
        monthName.contains("Março") -> 4
        monthName.contains("Abril") -> 3
        monthName.contains("Maio") -> 5
        else -> 2 // Junho
    }

    for (i in 1..days) {
        // Adiciona valores simulados de corrida a cada X dias
        val distance = if (i % step == 0) {
            (2..8).random() + (0..9).random() / 10f
        } else if (i % 7 == 0) {
            10.5f // Longão do domingo
        } else {
            0f // Não treinou
        }
        list.add(DailyRunProgress(day = i, distanceKm = distance))
    }
    return list
}