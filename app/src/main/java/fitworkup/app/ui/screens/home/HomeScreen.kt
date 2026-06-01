package com.fitworkup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HomeScreen — placeholder
 *
 * Esta tela será desenvolvida na próxima etapa com:
 * - Card de XP e nível
 * - Streak
 * - Botão de iniciar atividade
 * - Nudge da IA
 * - Bottom Navigation
 */
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏠",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "FitWorkUp",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE0271A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Login realizado com sucesso!\nHome em construção...",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
