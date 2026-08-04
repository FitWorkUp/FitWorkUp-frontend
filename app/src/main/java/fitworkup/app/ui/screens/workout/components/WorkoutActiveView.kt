package com.fitworkup.app.ui.screens.workout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitworkup.app.ui.viewmodel.WorkoutUiState

@Composable
fun WorkoutActiveView(
    uiState: WorkoutUiState,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Seção do Cronômetro, Distância, Pace e Moedas (Com cores responsivas ao Dark Mode)
            WorkoutMetricsSection(uiState = uiState)

            // Ação de Finalizar Treino
            Button(
                onClick = onFinishWorkout,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Finalizar Treino")
                }
            }
        }
    }
}