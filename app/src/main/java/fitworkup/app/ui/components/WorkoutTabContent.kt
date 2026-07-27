package com.fitworkup.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.ui.components.MiniMapa
import com.fitworkup.app.ui.components.MonthlyProgressCard

@Composable
fun WorkoutTabContent(
    userName: String = "Atleta",
    fitCoins: Int = 0,
    currentSteps: Int = 0,
    dailyStepGoal: Int = 10000,
    totalKmToday: Float = 0.0f,
    caloriesBurnedToday: Int = 0,
    onStartWorkout: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showGoalBottomSheet by remember { mutableStateOf(false) }

    // Cálculo dinâmico do progresso diário de passos (de 0.0 a 1.0)
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", modifier = Modifier.padding(8.dp))
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

            // 2. ANEL DE META DIÁRIA DE PASSOS (KPI CENTRAL REESTRUTURADO)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Trilho de Fundo do Círculo
                CircularProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round
                )
                // Barra de Progresso Real
                CircularProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Round
                )

                // Texto Interno do Anel
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
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

            // 3. DUETO DE PERFORMANCE COM DADOS REAIS
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

            // 4. GRÁFICO HISTÓRICO MENSAL
            MonthlyProgressCard()

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