package com.fitworkup.app.ui.screens.profile.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitworkup.app.R

data class AvatarOption(
    val key: String,
    @DrawableRes val drawableRes: Int
)

val availableAvatars = listOf(
    AvatarOption("ICONWOMAN1", R.drawable.iconwoman1_transparent),
    AvatarOption("ICONWOMAN2", R.drawable.iconwoman2_transparent),
    AvatarOption("ICONWOMAN3", R.drawable.iconwoman3_transparent),
    AvatarOption("ICONWOMAN4", R.drawable.iconwoman4_transparent),
    AvatarOption("ICONMAN1", R.drawable.iconman1_transparent),
    AvatarOption("ICONMAN2", R.drawable.iconman2),
    AvatarOption("ICONMAN3", R.drawable.iconman3_transparent),
    AvatarOption("ICONMAN4", R.drawable.iconman4_transparent)
)

@DrawableRes
fun avatarDrawable(avatarKey: String?): Int = availableAvatars
    .firstOrNull { it.key.equals(avatarKey, ignoreCase = true) }
    ?.drawableRes
    ?: R.drawable.iconman1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerBottomSheet(
    currentAvatarKey: String,
    isSaving: Boolean,
    onSave: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedKey by remember(currentAvatarKey) { mutableStateOf(currentAvatarKey) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Escolha seu avatar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "A moldura e o título continuam equipados separadamente.",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(availableAvatars, key = { it.key }) { avatar ->
                    val selected = selectedKey.equals(avatar.key, ignoreCase = true)
                    Image(
                        painter = painterResource(avatar.drawableRes),
                        contentDescription = "Selecionar ${avatar.key}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .border(
                                width = if (selected) 4.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape
                            )
                            .clickable(enabled = !isSaving) { selectedKey = avatar.key }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onSave(selectedKey) },
                enabled = !isSaving && selectedKey.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("SALVAR AVATAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
