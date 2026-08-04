package com.fitworkup.app.domain.model

data class OnboardingHighlight(
    val value: String,
    val label: String
)

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val highlightA: OnboardingHighlight? = null,
    val highlightB: OnboardingHighlight? = null,
    val checkItems: List<String> = emptyList()
)

object OnboardingData {
    val pages = listOf(
        OnboardingPage(
            emoji = "🏃",
            title = "Seu esforço real,\nrecompensado de verdade",
            description = "Cada passo conta. Corridas e caminhadas\nviram XP, moedas e status no ranking.",
            highlightA = OnboardingHighlight("+150", "XP/km corrida"),
            highlightB = OnboardingHighlight("+100", "XP/km caminhada")
        ),
        OnboardingPage(
            emoji = "🏆",
            title = "Compete, sobe no ranking\ne exibe seu status",
            description = "Ranking semanal ao vivo. Desbloqueie\nbordas e títulos exclusivos."
        ),
        OnboardingPage(
            emoji = "🛡️",
            title = "Anti-fraude inteligente.\nSó esforço real vale.",
            description = "GPS + acelerômetro validam cada\nmetro percorrido. Nada de trapaça.",
            checkItems = listOf(
                "GPS confirma deslocamento real",
                "Acelerômetro valida cadência de passos",
                "Detecção de veículo e fraude bloqueada"
            )
        )
    )
}