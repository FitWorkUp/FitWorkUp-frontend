package com.example.fitworkup.ui.theme.home

import ads_mobile_sdk.h1
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color

@Composable
fun HeroSection(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(brush = Brush.verticalGradient(listOf(Color(0xFF0B3B2E), Color(0xFF0F9D58)))) // substitua por imagem de background se quiser
            .height(600.dp) // ajuste para equivaler a 100vh; em Compose use constraints do container
    ) {
        // Overlay (escurecer)
        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp, top = 80.dp)
                .widthIn(max = 800.dp)
        ) {
            Text("FitWorkUp", style = MaterialTheme.typography.h1.copy(color = RedPrimary, fontSize = 48.sp))
            Spacer(Modifier.height(8.dp))
            Text("Seu treino começa aqui!", style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(Modifier.height(16.dp))
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(backgroundColor = RedPrimary)) {
                Text("Começar agora", color = Color.White)
            }
        }
    }
}
