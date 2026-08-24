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
            title = "Registre sua atividade",
            description = "Inicie uma caminhada ou corrida e escolha como deseja treinar.",
            checkItems = listOf(
                "Treine sozinho ou participe de uma sala em grupo",
                "Use o estilo livre ou defina uma meta de distância",
                "Pause e encerre a atividade quando precisar"
            )
        ),
        OnboardingPage(
            emoji = "📍",
            title = "Acompanhe seu progresso",
            description = "Durante o treino, acompanhe os dados coletados pelo seu celular.",
            checkItems = listOf(
                "Veja distância, duração e passos validados",
                "Acompanhe sua meta e quanto falta para concluí-la",
                "Consulte o percurso e o histórico de atividades"
            )
        ),
        OnboardingPage(
            emoji = "🏆",
            title = "Evolua com seu esforço",
            description = "Atividades validadas contribuem para sua progressão no FitWorkUp.",
            checkItems = listOf(
                "Ganhe experiência e FitCoins conforme as regras da atividade",
                "Desbloqueie conquistas ao atingir novas marcas",
                "Use FitCoins em itens e personalizações da loja"
            )
        ),
        OnboardingPage(
            emoji = "🤝",
            title = "Treine com outras pessoas",
            description = "Use os recursos sociais para acompanhar e incentivar seus amigos.",
            checkItems = listOf(
                "Adicione amigos e visite os perfis das suas conexões",
                "Compare passos validados no ranking semanal",
                "Crie uma sala ou entre em uma atividade com código"
            )
        )
    )
}
