package com.fitworkup.app.domain.model


data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val highlightA: String,
    val highlightB: String,
    val highlightLabelA: String,
    val highlightLabelB: String,
    val checkItems: List<String>
)

val pages = listOf(
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