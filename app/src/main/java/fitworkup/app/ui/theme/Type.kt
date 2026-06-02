package com.fitworkup.app.ui.theme // Certifique-se de que tem o "com." aqui também

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// O seu objeto precisa se chamar exatamente "Typography" e ser do tipo Material 3
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // Seus outros estilos (headlineMedium, titleLarge, etc.)
)