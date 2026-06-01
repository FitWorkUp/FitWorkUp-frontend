package com.fitworkup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fitworkup.app.navigation.NavGraph
import com.fitworkup.app.ui.screens.home.HomeScreen
import fitworkup.app.ui.theme.FitWorkUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitWorkUpTheme {
                NavGraph()   // importar de com.fitworkup.app.navigation
            }
        }
    }
}


