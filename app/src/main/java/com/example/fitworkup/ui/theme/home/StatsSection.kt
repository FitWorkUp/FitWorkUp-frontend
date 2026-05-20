package com.example.fitworkup.ui.theme.home

import ads_mobile_sdk.h1
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import  androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StatsSection() {
    val stats = listOf("100+" to "Treinos cadastrados", "50+" to "Desafios ativos", "90%" to "Taxa de adesão")
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        stats.forEach { (value, label) ->
            Surface(modifier = Modifier.weight(1f).padding(8.dp), shape = RoundedCornerShape(16.dp), elevation = 6.dp, color = CardBg) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, style = MaterialTheme.typography.h1.copy(color = TextTitle, fontSize = 32.sp))
                    Spacer(Modifier.height(8.dp))
                    Text(label.uppercase(), color = StatsLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
