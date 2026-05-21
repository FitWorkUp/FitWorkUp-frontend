package com.example.fitworkup.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HeroSection(
    onGetStartedClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF0B3B2E), Color(0xFF0F9D58))))
    ) {
        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            Text("FitWorkUp", style = MaterialTheme.typography.displayLarge.copy(color = Color.White))
            Text("Seu treino começa aqui!", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.White))

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onGetStartedClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Começar agora")
            }
        }
    }
}