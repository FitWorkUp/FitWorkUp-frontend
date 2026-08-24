package com.fitworkup.app.ui.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitworkup.app.domain.model.BadgeItem

@Composable
fun ProfileBadgesSection(
    badges: List<BadgeItem>,
    modifier: Modifier = Modifier
) {
    var selectedBadge by remember { mutableStateOf<BadgeItem?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Conquistas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (badges.isEmpty()) {
            Text(
                text = "Nenhuma conquista para exibir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                badges.chunked(4).forEach { badgeRow ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        badgeRow.forEach { badge ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                BadgeIcon(
                                    badge = badge,
                                    onClick = { selectedBadge = badge }
                                )
                            }
                        }
                        repeat(4 - badgeRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    selectedBadge?.let { badge ->
        AchievementDetailsDialog(
            badge = badge,
            onDismiss = { selectedBadge = null }
        )
    }
}

@Composable
private fun BadgeIcon(
    badge: BadgeItem,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = CircleShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.size(64.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            AchievementIcon(
                badge = badge,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}

@Composable
private fun AchievementIcon(
    badge: BadgeItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconResource = remember(badge.iconName) {
        badge.iconName
            ?.takeIf { it.isNotBlank() }
            ?.let { context.resources.getIdentifier(it, "drawable", context.packageName) }
            ?: 0
    }

    if (iconResource != 0) {
        Image(
            painter = painterResource(iconResource),
            contentDescription = "Ícone da conquista ${badge.name}",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = if (badge.unlocked) "🏆" else "🔒",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun AchievementDetailsDialog(
    badge: BadgeItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            AchievementIcon(
                badge = badge,
                modifier = Modifier.size(72.dp)
            )
        },
        title = {
            Text(
                text = badge.name,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (badge.xpReward > 0 || badge.fitcoinsReward > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "+${badge.xpReward} XP  •  +${badge.fitcoinsReward} FitCoins",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
