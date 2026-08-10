package com.fitworkup.app.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.domain.model.UserProfile

@Composable
fun ProfileTopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Meu Perfil",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ProfileHeaderInfo(profile: UserProfile?) {
    val avatarBorderColor = when {
        profile?.avatarBorder?.contains("Rubi", ignoreCase = true) == true -> Color(0xFFD32F2F)
        profile?.avatarBorder?.contains("Esmeralda", ignoreCase = true) == true -> Color(0xFF2E7D32)
        profile?.avatarBorder?.contains("Lendária", ignoreCase = true) == true -> Color(0xFFFFB300)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(4.dp, avatarBorderColor, CircleShape)
                    .padding(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("LVL ${profile?.level ?: 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = profile?.name ?: "Carregando...", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = "Título: ${profile?.title ?: "NOVATO"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ProfileXpProgressCard(profile: UserProfile?) {
    val currentXp = profile?.currentXp ?: 0
    val maxXp = (profile?.maxXp ?: 1).coerceAtLeast(1)
    val progressValue = (currentXp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progresso de XP", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$currentXp / $maxXp XP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ProfileFriendsHubCard(
    friendCount: Int,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Meus Amigos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "$friendCount conexões no FitWorkUp", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
