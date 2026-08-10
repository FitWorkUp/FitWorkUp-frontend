package com.fitworkup.app.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitworkup.app.domain.model.BadgeItem

@Composable
fun ProfileBadgesSection(
    badges: List<BadgeItem>,
    modifier: Modifier = Modifier
) {
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
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(badges) { badge ->
                    BadgeCard(badge = badge)
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: BadgeItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .width(130.dp)
        ) {
            Text(
                text = if (badge.unlocked) "🏆" else "🔒",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            if (badge.xpReward > 0 || badge.fitcoinsReward > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+${badge.xpReward} XP • +${badge.fitcoinsReward} FC",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
