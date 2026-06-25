package com.fitworkup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// 1. IMPORTANTE: Esse import precisa estar aqui para controlar a Splash nativa
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fitworkup.app.navigation.NavGraph
import com.fitworkup.app.ui.theme.FitWorkUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 2. O SEGREDO: Ativa a transição da Splash nativa para o tema correto (Sem Barra)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            FitWorkUpTheme {
                NavGraph()
            }
        }
    }
}