package com.fitworkup.app.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitworkup.app.domain.model.LeagueInfo
import com.fitworkup.app.domain.model.RankingUiState
import com.fitworkup.app.ui.screens.ranking.components.RankingViewModel

@Composable
fun RankingTabRoute(
    viewModel: RankingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    RankingTabContent(
        uiState = uiState,
        onRetry = { viewModel.loadRanking() }
    )
}

@Composable
fun RankingTabContent(
    uiState: RankingUiState,
    onRetry: () -> Unit
) {
    when (uiState) {
        is RankingUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is RankingUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tentar Novamente")
                }
            }
        }
        is RankingUiState.Success -> {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ─── CABEÇALHO DA LIGA
                LeagueHeaderCard(leagueInfo = uiState.leagueInfo)

                Spacer(modifier = Modifier.height(24.dp))

                // ─── O PÓDIO
                Text(
                    text = "Líderes da Semana",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                val first = uiState.topThree.find { it.rank == 1 }
                val second = uiState.topThree.find { it.rank == 2 }
                val third = uiState.topThree.find { it.rank == 3 }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    second?.let {
                        PodiumItem(name = it.name, xp = "${it.xp} XP", rank = "2", height = 110, color = Color(0xFFC0C0C0))
                    }
                    first?.let {
                        PodiumItem(name = it.name, xp = "${it.xp} XP", rank = "1", height = 140, color = Color(0xFFFFD700))
                    }
                    third?.let {
                        PodiumItem(name = it.name, xp = "${it.xp} XP", rank = "3", height = 95, color = Color(0xFFCD7F32))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ─── SUA POSIÇÃO
                uiState.currentUser?.let { user ->
                    Text(
                        text = "Sua Posição",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LeaderboardRow(
                        rank = user.rank.toString(),
                        name = "${user.name} (Você)",
                        xp = "${user.xp} XP",
                        isCurrentUser = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ─── OUTROS ATLETAS
                if (uiState.otherAthletes.isNotEmpty()) {
                    Text(
                        text = "Demais Atletas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.otherAthletes.forEach { athlete ->
                        if (!athlete.isCurrentUser) {
                            LeaderboardRow(
                                rank = athlete.rank.toString(),
                                name = athlete.name,
                                xp = "${athlete.xp} XP"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueHeaderCard(leagueInfo: LeagueInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = "Liga Atleta",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${leagueInfo.title} • ${leagueInfo.group}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Termina em: ${leagueInfo.timeRemaining}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun PodiumItem(name: String, xp: String, rank: String, height: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = color)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = xp, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LeaderboardRow(rank: String, name: String, xp: String, isCurrentUser: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rank,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp),
                color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(2).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = xp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}