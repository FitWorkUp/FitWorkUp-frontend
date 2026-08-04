package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.ui.components.MiniMapa
import com.fitworkup.app.ui.components.MonthlyProgressCard
import com.fitworkup.app.ui.screens.dashboard.DashboardViewModel
import com.fitworkup.app.ui.screens.home.components.PerformanceCard
import com.fitworkup.app.ui.screens.home.components.WorkoutGoalBottomSheet

@Composable
fun WorkoutCountdownView(countdown: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdown > 0) countdown.toString() else "VAI!",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pronto para subir de nível?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "O sistema anti-fraude está ativo.\nSeu esforço será 100% validado.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Calibrando GPS e Acelerômetro...", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        LinearProgressIndicator(
            progress = { (3 - countdown) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun WorkoutTabContent(
    userName: String = "Atleta",
    fitCoins: Int = 0,
    currentSteps: Int = 0,
    dailyStepGoal: Int = 10000,
    totalKmToday: Float = 0.0f,
    caloriesBurnedToday: Int = 0,
    onStartWorkout: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showGoalBottomSheet by remember { mutableStateOf(false) }

    // Progresso do anel de passos (0.0f a 1.0f)
    val stepProgress = if (dailyStepGoal > 0) {
        (currentSteps.toFloat() / dailyStepGoal.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. CABEÇALHO DINÂMICO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Olá, $userName!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "🪙 $fitCoins FitCoins",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. TÓPICO 1: ANEL DE META DIÁRIA DE PASSOS
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Companion.Round
                )
                CircularProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Companion.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currentSteps",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "/ $dailyStepGoal passos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. TÓPICO 2 E 3: DUETO DE PERFORMANCE (DISTÂNCIA E CALORIAS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PerformanceCard(
                    title = "Distância Hoje",
                    value = String.format("%.1f km", totalKmToday),
                    modifier = Modifier.weight(1f)
                )
                PerformanceCard(
                    title = "Calorias Hoje",
                    value = "$caloriesBurnedToday kcal",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. GRÁFICO HISTÓRICO MENSAL INTEGRADO AO VIEWMODEL
            MonthlyProgressCard(
                monthlyData = dashboardState.monthlyProgress,
                selectedYearMonth = dashboardState.selectedYearMonth,
                focusedDay = dashboardState.focusedDay,
                dayActivities = dashboardState.focusedDayActivities,
                dayTotalKm = dashboardState.focusedDayTotalKm,
                onMonthChanged = dashboardViewModel::onMonthChanged,
                onDayFocused = dashboardViewModel::onDayFocused
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. MINI MAPA DO ÚLTIMO PERCURSO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Último Percurso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        MiniMapa()
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // BOTÃO DE AÇÃO FIXO
        Button(
            onClick = { showGoalBottomSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("INICIAR ATIVIDADE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (showGoalBottomSheet) {
            WorkoutGoalBottomSheet(
                onDismissRequest = { showGoalBottomSheet = false },
                onStartWorkout = { _, _, _ ->
                    showGoalBottomSheet = false
                    onStartWorkout()
                }
            )
        }
    }
}