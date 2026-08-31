package com.fitworkup.app.ui.screens.store

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.R
import com.fitworkup.app.domain.model.StoreItem
import com.fitworkup.app.ui.components.RemoteContentError
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StorePoints(viewModel: StoreViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedItem by remember { mutableStateOf<StoreItem?>(null) }

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(uiState.notification) {
        uiState.notification?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            uiState.errorMessage != null && uiState.items.isEmpty() -> RemoteContentError(
                onRetry = viewModel::refresh,
                modifier = Modifier.align(Alignment.Center),
                title = "Loja indisponível"
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BalanceCard(uiState.balance)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Recompensas disponíveis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                }
                if (uiState.items.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Nenhum item disponível no momento.",
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(uiState.items, key = StoreItem::id) { item ->
                    StoreItemCard(
                        item = item,
                        isProcessing = uiState.processingItemId == item.id,
                        onPurchase = { selectedItem = item },
                        onEquip = { viewModel.equip(item) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    selectedItem?.let { item ->
        StorePurchaseDialog(
            item = item,
            balance = uiState.balance,
            onDismiss = { selectedItem = null },
            onConfirm = {
                selectedItem = null
                viewModel.purchase(item)
            }
        )
    }
}

@Composable
private fun BalanceCard(balance: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "Seu saldo atual",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "$balance FitCoins",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItem,
    isProcessing: Boolean,
    onPurchase: () -> Unit,
    onEquip: () -> Unit
) {
    val activeUntilLabel = item.activeUntil?.let(::formatActiveUntil)

    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StoreItemVisual(item = item, size = 54.dp, emojiSize = 27.sp)

            Spacer(Modifier.height(10.dp))
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.category.equals("BOOST", ignoreCase = true)) {
                    Text(
                    text = activeUntilLabel ?: "${item.multiplier?.toInt() ?: 2}x • ${item.durationMinutes ?: 0} min",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activeUntilLabel != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Text(
                text = "${item.priceInCoins} FitCoins",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            when {
                isProcessing -> CircularProgressIndicator(Modifier.size(30.dp), strokeWidth = 2.dp)
                item.isEquipped -> StoreOutlinedButton("Em uso", false) {}
                item.isPurchased && !item.repeatable -> StoreOutlinedButton("Equipar", true, onEquip)
                else -> Button(
                    onClick = onPurchase,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (activeUntilLabel != null) "Estender" else if (item.repeatable) "Ativar" else "Resgatar",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StorePurchaseDialog(
    item: StoreItem,
    balance: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isBoost = item.category.equals("BOOST", ignoreCase = true)
    val hasEnoughBalance = balance >= item.priceInCoins
    val missingCoins = (item.priceInCoins - balance).coerceAtLeast(0)
    val activeUntilLabel = item.activeUntil?.let(::formatActiveUntil)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            StoreItemVisual(item = item, size = 62.dp, emojiSize = 31.sp)
        },
        title = {
            Text(
                text = item.name,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = item.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isBoost) {
                    PurchaseDetailRow(
                        label = "Efeito",
                        value = storeEffectLabel(item)
                    )
                    PurchaseDetailRow(
                        label = "Multiplicador",
                        value = multiplierLabel(item.multiplier)
                    )
                    PurchaseDetailRow(
                        label = "Duração",
                        value = "${item.durationMinutes ?: 0} minutos"
                    )
                    activeUntilLabel?.let { activeUntil ->
                        Text(
                            text = "$activeUntil. A nova compra acrescentará mais ${item.durationMinutes ?: 0} minutos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                HorizontalDivider()

                PurchaseDetailRow(
                    label = "Preço",
                    value = "${item.priceInCoins} FitCoins",
                    valueColor = MaterialTheme.colorScheme.primary
                )
                PurchaseDetailRow(
                    label = "Seu saldo",
                    value = "$balance FitCoins"
                )

                if (!hasEnoughBalance) {
                    Text(
                        text = "Faltam $missingCoins FitCoins para concluir esta compra.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = if (isBoost) {
                            "O efeito será ativado imediatamente após a confirmação."
                        } else {
                            "O item ficará disponível para ser equipado no seu perfil."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = hasEnoughBalance
            ) {
                Text(confirmPurchaseLabel(item))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PurchaseDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

private fun storeEffectLabel(item: StoreItem): String = when (item.effectType?.uppercase()) {
    "XP_MULTIPLIER" -> "Bônus de XP"
    "FITCOINS_MULTIPLIER" -> "Bônus de FitCoins"
    else -> "Bônus temporário"
}

private fun multiplierLabel(multiplier: Double?): String {
    val safeMultiplier = multiplier ?: 1.0
    return if (safeMultiplier % 1.0 == 0.0) {
        "${safeMultiplier.toInt()}x"
    } else {
        "${safeMultiplier}x"
    }
}

private fun confirmPurchaseLabel(item: StoreItem): String = when {
    item.activeUntil != null -> "Estender por ${item.priceInCoins}"
    item.repeatable -> "Ativar por ${item.priceInCoins}"
    else -> "Resgatar por ${item.priceInCoins}"
}

@Composable
private fun StoreOutlinedButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

private fun formatActiveUntil(value: String): String = runCatching {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val localTime = Instant.parse(value).atZone(ZoneId.systemDefault()).format(formatter)
    "Ativo até $localTime"
}.getOrDefault("Bônus ativo")

@Composable
private fun StoreItemVisual(item: StoreItem, size: Dp, emojiSize: TextUnit) {
    val imageResource = storeItemImageResource(item)
    val containerSize = if (imageResource != null) size * 1.25f else size
    Box(
        modifier = Modifier
            .size(containerSize)
            .then(
                if (imageResource == null) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageResource != null) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.18f)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(text = item.iconEmoji, fontSize = emojiSize)
        }
    }
}

@DrawableRes
private fun storeItemImageResource(item: StoreItem): Int? {
    if (!item.category.equals("AVATAR_FRAME", ignoreCase = true)) return null

    return when (item.name.lowercase(Locale.ROOT)) {
        "moldura rubi" -> R.drawable.store_frame_red
        "moldura ametista" -> R.drawable.store_frame_purple
        "moldura esmeralda" -> R.drawable.store_frame_green
        "moldura lendária" -> R.drawable.store_frame_gold
        else -> null
    }
}
