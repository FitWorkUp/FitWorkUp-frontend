package com.fitworkup.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.ui.components.MonthlyProgressCard

@Composable
fun WorkoutTabContent(
    onStartWorkout: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showGoalBottomSheet by remember { mutableStateOf(false) }

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
            // 1. CABEÇALHO
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Olá, Atleta!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text("🪙 1.450 FitCoins", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
                }
            }

            // 2. KPI CENTRAL
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                CircularProgressIndicator(
                    progress = { 0.8f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round // ✨ Deixa as pontas do arco arredondadas
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. DUETO DE PERFORMANCE (Usando o card isolado)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PerformanceCard("Distância", "3.2 km", Modifier.weight(1f))
                PerformanceCard("Calorias", "512 kcal", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. GRÁFICO HISTÓRICO
            MonthlyProgressCard()

            Spacer(modifier = Modifier.height(88.dp))
        }

        // BOTÃO DE AÇÃO FIXO
        Button(
            onClick = { showGoalBottomSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding() // 🟢 ADICIONADO: Garante que o botão fique acima da barra de gestos do celular
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