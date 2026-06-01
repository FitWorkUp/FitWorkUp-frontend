package com.fitworkup.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Cores internas (dark background fixo nesta tela) ───────────────────────
private val SplashBg     = Color(0xFF1A1730)
private val SplashPurple = Color(0xFF534AB7)
private val SplashLight  = Color(0xFFEEEDFE)
private val SplashMuted  = Color(0xFFAFA9EC)

// ─── Constantes ─────────────────────────────────────────────────────────────
private const val SPLASH_DELAY_MS = 2200L

/**
 * SplashScreen
 *
 * Exibida por ~2 segundos ao abrir o app.
 * Após o delay, chama [onNavigate]:
 *   - se usuário já logou → "home"
 *   - se é novo          → "onboarding"
 *
 * Por enquanto sempre navega para onboarding.
 * Quando tiver DataStore/Auth, troque a lógica dentro do LaunchedEffect.
 */
@Composable
fun SplashScreen(
    onNavigate: (route: String) -> Unit
) {
    // Animações de entrada
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        // Anima logo aparecendo
        alpha.animateTo(1f, animationSpec = tween(600))
        scale.animateTo(1f, animationSpec = tween(600))

        // Aguarda e navega
        delay(SPLASH_DELAY_MS)

        // TODO: trocar por verificação de sessão no DataStore
        // val jaLogou = dataStore.data.map { it[USER_TOKEN_KEY] != null }.first()
        // onNavigate(if (jaLogou) "home" else "onboarding")
        onNavigate("onboarding")
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // Ícone do app
            LogoIcon()

            Spacer(modifier = Modifier.height(4.dp))

            // Nome
            Text(
                text = "FitWorkUp",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = SplashLight,
                letterSpacing = 0.5.sp
            )

            // Tagline
            Text(
                text = "MOVA-SE. EVOLUA. DOMINE.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = SplashMuted,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indicador de loading (3 pontos pulsantes)
            LoadingDots()
        }
    }
}

// ── Componentes internos ─────────────────────────────────────────────────────

@Composable
private fun LogoIcon() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(SplashPurple, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder do ícone — substitua por Image(painterResource(...))
        // quando tiver o asset final
        Text(
            text = "FW",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = SplashLight
        )
    }
}

@Composable
private fun LoadingDots() {
    val dot1Alpha = remember { Animatable(1f) }
    val dot2Alpha = remember { Animatable(0.4f) }
    val dot3Alpha = remember { Animatable(0.2f) }

    LaunchedEffect(Unit) {
        // Anima os pontos em loop
        while (true) {
            dot1Alpha.animateTo(0.2f, tween(400))
            dot2Alpha.animateTo(1f,   tween(400))
            dot3Alpha.animateTo(0.4f, tween(400))

            dot1Alpha.animateTo(0.4f, tween(400))
            dot2Alpha.animateTo(0.2f, tween(400))
            dot3Alpha.animateTo(1f,   tween(400))

            dot1Alpha.animateTo(1f,   tween(400))
            dot2Alpha.animateTo(0.4f, tween(400))
            dot3Alpha.animateTo(0.2f, tween(400))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { anim ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(anim.value)
                    .background(SplashPurple, RoundedCornerShape(50))
            )
        }
    }
}
