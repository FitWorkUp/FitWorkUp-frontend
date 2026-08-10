package com.fitworkup.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MS = 2000L

private val MOTIVATIONAL_QUOTES = listOf(
    "SUBA DE NÍVEL A CADA QUILÔMETRO ⚡",
    "CADA PASSO CONTA PARA O SEU RANKING 🏆",
    "TRANSFORME SUAS PASSADAS EM FITCOINS 🪙",
    "SUPERANDO SEUS LIMITES HOJE 🔥",
    "SUA MELHOR VERSÃO EM CONSTRUÇÃO 🏃",
    "CONSTÂNCIA GERA RESULTADOS 💪"
)

@Composable
fun SplashScreen(
    onNavigate: (route: String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }
    val randomQuote = remember { MOTIVATIONAL_QUOTES.random() }

    LaunchedEffect(Unit) {
        // Animação suave de entrada (Fade-in + Scale)
        alpha.animateTo(1f, animationSpec = tween(700))
        scale.animateTo(1f, animationSpec = tween(500))

        delay(SPLASH_DELAY_MS)
    }

    LaunchedEffect(destination) {
        val route = destination ?: return@LaunchedEffect
        delay(SPLASH_DELAY_MS)
        onNavigate(route)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(24.dp)
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // 👟 Imagem do Tênis de Corrida substituindo o bloco antigo com o emoji ⚡
            Image(
                painter = painterResource(id = R.drawable.corrida),
                contentDescription = "FitWorkUp Logo",
                modifier = Modifier.size(160.dp)
            )

            Text(
                text = "FitWorkUp",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = randomQuote,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
