package com.fitworkup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fitworkup.app.ui.theme.FitWorkUpTheme
import com.fitworkup.app.ui.screens.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitWorkUpTheme { // seu tema customizado
                HomeScreen()   // chama a tela Home
            }
        }
    }
}


