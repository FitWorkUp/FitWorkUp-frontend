package com.fitworkup.app.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── 1. CARD DO ATLETA (Status do Jogo) ─────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder de Avatar. No futuro, pode ter a borda conquistada no ranking!
            Text(text = "🛡️", fontSize = 42.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Atleta_Fit",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Nível 12", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(text = "🪙 1.450 Moedas", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── 2. SEÇÃO: DEFINIÇÃO ───────────────────────────────────────────────
        SectionHeader(title = "DEFINIÇÃO")

        ProfileMenuItem(
            icon = Icons.Outlined.DirectionsRun,
            iconContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            iconColor = MaterialTheme.colorScheme.primary,
            title = "Configurações do treino",
            subtitle = "Timer, alertas de voz e calibração de sensores",
            onClick = { /* TODO */ }
        )

        ProfileMenuItem(
            icon = Icons.Outlined.Settings,
            iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Definições gerais",
            subtitle = "Dados da conta, segurança e tema",
            onClick = { /* TODO */ }
        )

        ProfileMenuItem(
            icon = Icons.Outlined.Language,
            iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Opção de Língua",
            subtitle = "Português (BR)",
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── 3. SEÇÃO: AJUDE-NOS ───────────────────────────────────────────────
        SectionHeader(title = "AJUDE-NOS")

        ProfileMenuItem(
            icon = Icons.Outlined.StarOutline,
            iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Nos Avalie",
            subtitle = "Apoie o FitWorkUp na Play Store",
            onClick = { /* TODO */ }
        )

        ProfileMenuItem(
            icon = Icons.Outlined.Share,
            iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Compartilhe com um amigo",
            subtitle = "Convide atletas e ganhe bônus de Moedas",
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        letterSpacing = 1.sp
    )
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        headlineContent = { Text(text = title, fontWeight = FontWeight.Medium, fontSize = 15.sp) },
        supportingContent = { Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconContainerColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}