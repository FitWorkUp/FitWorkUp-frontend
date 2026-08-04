package com.fitworkup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurações",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para o Perfil"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ─── 1. SEÇÃO: DEFINIÇÃO ───────────────────────────────────────────────
            SectionHeader(title = "DEFINIÇÃO")

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Outlined.DirectionsRun,
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
                subtitle = "Dados da conta e segurança",
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

            // ─── 2. SEÇÃO: AJUDE-NOS ───────────────────────────────────────────────
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
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
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
    iconContainerColor: Color,
    iconColor: Color,
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