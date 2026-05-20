package com.example.fitworkup.ui.theme.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Feature
import com.example.fitworkup.ui.theme.FitWorkUpTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.plus
@Composable
fun FeaturesSection() {
    val features = listOf(
        Feature("🏋️", "Treinos Personalizados", "Planos adaptados ao seu perfil."),
        // ... demais
    )
    val infinite = remember { features + features }
    val listState = rememberLazyListState()

    // Auto-scroll simples
    LaunchedEffect(Unit) {
        while (true) {
            // scroll um item a cada 2s (ajuste)
            delay(2000)
            val next = (listState.firstVisibleItemIndex + 1) % infinite.size
            listState.animateScrollToItem(next)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Text("Por que usar o FitWorkUp?", style = MaterialTheme.typography.h2, color = RedPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))
        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(infinite) { f ->
                Card(modifier = Modifier.width(280.dp).height(140.dp), backgroundColor = CardBg) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(f.icon, fontSize = 28.sp)
                        Text(f.title, fontWeight = FontWeight.SemiBold, color = TextSobre)
                        Text(f.desc, fontSize = 12.sp, color = TextSobre)
                    }
                }
            }
        }
    }
}
