package com.fitworkup.app.ui.screens.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitworkup.app.domain.model.FriendItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListBottomSheet(
    friendsList: List<FriendItem>,
    onDismissRequest: () -> Unit,
    onFriendClick: (FriendItem) -> Unit,
    onRemoveFriend: (String) -> Unit
) {
    // 🟢 Estado para controlar qual amigo está prestes a ser excluído (controla a exibição do AlertDialog)
    var friendToDelete by remember { mutableStateOf<FriendItem?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Minhas Conexões (${friendsList.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (friendsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum amigo adicionado ainda.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(friendsList) { friend ->
                        FriendRowItem(
                            friend = friend,
                            onClick = { onFriendClick(friend) },
                            onRemoveClick = { friendToDelete = friend } // 🟢 Abre o diálogo em vez de excluir direto
                        )
                    }
                }
            }
        }
    }

    // 🟢 AlertDialog de Confirmação de Exclusão
    if (friendToDelete != null) {
        AlertDialog(
            onDismissRequest = { friendToDelete = null },
            title = { Text("Remover amigo") },
            text = {
                Text("Tem certeza de que deseja remover ${friendToDelete?.name} das suas conexões?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        friendToDelete?.let {
                            onRemoveFriend(it.id)
                        }
                        friendToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { friendToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FriendRowItem(friend: FriendItem, onClick: () -> Unit, onRemoveClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(avatarDrawable(friend.avatarKey)),
                        contentDescription = "Avatar de ${friend.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = friend.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "@${friend.tag} • LVL ${friend.level}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover amigo",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
