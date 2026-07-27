package com.fitworkup.app.ui.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RankingTabContent() {
    val scrollState = rememberScrollState()

    // ─── 1. CONTAINER PAI (Organiza a tela verticalmente)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ─── 2. CABEÇALHO DA LIGA (Card Informativo)
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
                        text = "Liga Ouro • Grupo 4",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Termina em: 3 dias e 04h",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── 3. O PÓDIO (ROW com 3 COLUMNS alinhadas na base)
        Text(
            text = "Líderes da Semana",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom // Garante que as colunas apoiam-se na mesma linha
        ) {
            // 2º LUGAR
            PodiumItem(name = "Carlos M.", xp = "2.450 XP", rank = "2", height = 110, color = Color(0xFFC0C0C0))

            // 1º LUGAR (Mais alto e em destaque)
            PodiumItem(name = "Ana Silva", xp = "3.120 XP", rank = "1", height = 140, color = Color(0xFFFFD700))

            // 3º LUGAR
            PodiumItem(name = "Pedro R.", xp = "2.100 XP", rank = "3", height = 95, color = Color(0xFFCD7F32))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── 4. RESTO DA LISTA (Múltiplas Rows consecutivas)
        Text(
            text = "Sua Posição",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Linha com a posição atual do utilizador conectado (Destaque)
        LeaderboardRow(rank = "7", name = "Você (Ronaldo)", xp = "1.250 XP", isCurrentUser = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Próximos Atletas",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Outros competidores abaixo
        LeaderboardRow(rank = "4", name = "Julia Lima", xp = "1.890 XP")
        Spacer(modifier = Modifier.height(8.dp))
        LeaderboardRow(rank = "5", name = "Marcos V.", xp = "1.640 XP")
        Spacer(modifier = Modifier.height(8.dp))
        LeaderboardRow(rank = "6", name = "Lucas Dias", xp = "1.450 XP")
    }
}

// ─── COMPONENTE AUXILIAR PARA CADA PILAR DO PÓDIO ─────────────────────────
@Composable
private fun PodiumItem(name: String, xp: String, rank: String, height: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        // Avatar do topo
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = color)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Coluna/Bloco do pódio
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

        // Textos abaixo do pódio
        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = xp, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

// ─── COMPONENTE AUXILIAR PARA CADA LINHA DO RANKING ──────────────────────
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
            // Posição Numérica
            Text(
                text = rank,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp),
                color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            // Pequena "div" (Box) redonda imitando a foto de perfil
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

            // Nome do Atleta (ocupa o espaço restante disponível)
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            // Pontuação / XP
            Text(
                text = xp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}