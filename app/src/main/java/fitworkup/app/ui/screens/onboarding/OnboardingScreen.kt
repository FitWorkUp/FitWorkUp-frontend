package com.fitworkup.app.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─── Modelo de dados de cada slide ──────────────────────────────────────────
data class OnboardingPage(
    val emoji: String,          // placeholder visual — troque por ícone/ilustração
    val title: String,
    val description: String,
    val highlightA: String,     // mini card esquerdo (slide 1) ou vazio
    val highlightB: String,     // mini card direito (slide 1) ou vazio
    val highlightLabelA: String,
    val highlightLabelB: String,
    val checkItems: List<String> // checklist (slide 3) ou lista vazia
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🏃",
        title = "Seu esforço real,\nrecompensado de verdade",
        description = "Cada passo conta. Corridas e caminhadas\nviram XP, moedas e status no ranking.",
        highlightA = "+150", highlightLabelA = "XP/km corrida",
        highlightB = "+100", highlightLabelB = "XP/km caminhada",
        checkItems = emptyList()
    ),
    OnboardingPage(
        emoji = "🏆",
        title = "Compete, sobe no ranking\ne exibe seu status",
        description = "Ranking semanal ao vivo. Desbloqueie\nbordas e títulos exclusivos.",
        highlightA = "", highlightLabelA = "",
        highlightB = "", highlightLabelB = "",
        checkItems = emptyList()
    ),
    OnboardingPage(
        emoji = "🛡️",
        title = "Anti-fraude inteligente.\nSó esforço real vale.",
        description = "GPS + acelerômetro validam cada\nmetro percorrido. Nada de trapaça.",
        highlightA = "", highlightLabelA = "",
        highlightB = "", highlightLabelB = "",
        checkItems = listOf(
            "GPS confirma deslocamento real",
            "Acelerômetro valida cadência de passos",
            "Detecção de veículo e fraude bloqueada"
        )
    )
)

// ─── Cores ──────────────────────────────────────────────────────────────────
private val FitRed    = Color(0xFFE0271A)
private val RedLight  = Color(0xFFFFECEB)
private val GreenLight= Color(0xFFE1F5EE)
private val GreenDark = Color(0xFF085041)
private val GreenMid  = Color(0xFF0F6E56)
private val RedDark   = Color(0xFF791F1F)

/**
 * OnboardingScreen
 *
 * 3 slides com HorizontalPager.
 * [onNavigateToLogin] chamado ao fim do último slide.
 */
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        // ── Slides ──────────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        // ── Rodapé: dots + botão ─────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        ) {
            // Dots indicadores
            PagerDots(
                pageCount = pages.size,
                currentPage = pagerState.currentPage
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão principal
            val isLastPage = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLastPage) {
                        onNavigateToLogin()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FitRed)
            ) {
                Text(
                    text = if (isLastPage) "Criar minha conta" else "Próximo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Pular (só nos dois primeiros slides)
            if (!isLastPage) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Pular",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ── Conteúdo de cada slide ───────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícone / ilustração placeholder
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = page.emoji, fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Título
        Text(
            text = page.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Descrição
        Text(
            text = page.description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Conteúdo específico por slide
        when {
            // Slide 1: mini cards de XP
            page.highlightA.isNotEmpty() -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    XpCard(
                        value = page.highlightA,
                        label = page.highlightLabelA,
                        background = RedLight,
                        valueColor = RedDark,
                        labelColor = FitRed,
                        modifier = Modifier.weight(1f)
                    )
                    XpCard(
                        value = page.highlightB,
                        label = page.highlightLabelB,
                        background = GreenLight,
                        valueColor = GreenDark,
                        labelColor = GreenMid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Slide 2: preview do ranking (mock estático)
            page.emoji == "🏆" -> {
                RankingPreview()
            }

            // Slide 3: checklist anti-fraude
            page.checkItems.isNotEmpty() -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    page.checkItems.forEach { item ->
                        CheckItem(text = item)
                    }
                }
            }
        }
    }
}

// ── Sub-componentes ──────────────────────────────────────────────────────────

@Composable
private fun XpCard(
    value: String,
    label: String,
    background: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = valueColor)
        Text(text = label, fontSize = 11.sp, color = labelColor)
    }
}

@Composable
private fun RankingPreview() {
    val mockUsers = listOf(
        Triple("1", "VelocistaBR", "4.820 xp"),
        Triple("2", "RunnerK",     "3.110 xp"),
        Triple("3", "MarinaFit",   "2.740 xp")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mockUsers.forEach { (pos, nome, xp) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(pos, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = if (pos == "1") Color(0xFFBA7517)
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.width(16.dp))

                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(FitRed.copy(alpha = 0.15f), RoundedCornerShape(50))
                )

                Text(nome, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f))

                Text(xp, fontSize = 12.sp,
                    color = if (pos == "1") Color(0xFFBA7517)
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun CheckItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GreenLight)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(GreenMid, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
        Text(text, fontSize = 12.sp, color = GreenDark)
    }
}

@Composable
private fun PagerDots(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 20.dp else 8.dp,
                animationSpec = tween(250),
                label = "dot_width_$index"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .background(
                        if (isActive) FitRed else FitRed.copy(alpha = 0.25f),
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}
