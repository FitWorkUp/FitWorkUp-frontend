package com.fitworkup.app.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
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
fun StorePoints() {
    // Estado de rolagem para a loja não cortar em telas menores
    val scrollState = rememberScrollState()

    // ─── 1. CONTAINER PAI PRINCIPAL (Organiza tudo verticalmente)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ─── 2. BOX DE SALDO (Topo da tela)
        // Usamos Box aqui porque ele permite alinhar o ícone de fundo de forma livre
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "Seu Saldo Atual",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Moedas",
                        tint = Color(0xFFFFD700), // Cor Dourada
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1,450 FC",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Título da seção de itens
        Text(
            text = "Recompensas Disponíveis",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // ─── 3. LISTA DE ITENS DA LOJA (Múltiplas Columns/Cards)

        // ITEM 1: Multiplicador de XP
        StoreItemRow(
            title = "Multiplicador de XP (2h)",
            description = "Dobre o XP ganho nas suas corridas pelas próximas duas horas.",
            price = "300 FC",
            icon = Icons.Default.FlashOn,
            iconColor = Color(0xFFFF9800)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ITEM 2: Emblema Lendário
        StoreItemRow(
            title = "Emblema 'Velocidade da Luz'",
            description = "Exiba um fundo animado exclusivo no seu perfil e ranking semanal.",
            price = "800 FC",
            icon = Icons.Default.Star,
            iconColor = Color(0xFF9C27B0)
        )
    }
}

// ─── COMPONENTE AUXILIAR PARA CADA ITEM DA LOJA ──────────────────────────
@Composable
private fun StoreItemRow(
    title: String,
    description: String,
    price: String,
    annotation: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    // Usamos um Card (que por baixo é uma Column modificada) para agrupar as informações
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box redondo para o ícone do produto
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Column interna para alinhar os textos à esquerda e o botão embaixo
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = price, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

                    Button(
                        onClick = { /* Lógica de compra */ },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Resgatar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}