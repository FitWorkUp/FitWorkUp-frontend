package com.example.fitworkup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitworkup.ui.home.HomePage
import com.example.fitworkup.ui.theme.FitWorkUpTheme
import com.example.fitworkup.ui.theme.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilita o suporte a borda a borda (edge-to-edge)
        enableEdgeToEdge()
        
        setContent {
            FitWorkUpTheme {
                // Surface aplica a cor de fundo definida no tema
                Surface(color = MaterialTheme.colorScheme.background) {
                    
                    // Instancia o AuthViewModel no escopo da Activity
                    val authViewModel: AuthViewModel = viewModel()

                    // Exibe a HomePage. 
                    // Nota: Se você for implementar navegação entre telas, 
                    // considere configurar um NavHost aqui.
                    HomePage(
                        viewModel = authViewModel,
                        onNavigateToMenu = { /* TODO: Implement navigation */ },
                        onNavigateToLogin = { /* TODO: Implement navigation */ }
                    )
                }
            }
        }
    }
}
