package com.fitworkup.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitworkup.app.ui.screens.splash.SplashScreen
import com.fitworkup.app.ui.screens.onboarding.OnboardingScreen
import com.fitworkup.app.ui.screens.home.LoginScreen // 👈 IMPORT CORRIGIDO (estava .home)
import com.fitworkup.app.ui.screens.home.HomeScreen

// ─── Rotas Principais (Telas Cheias) ────────────────────────────────────────
object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val HOME        = "home"
}

/**
 * NavGraph
 *
 * Ponto central de navegação do FitWorkUp.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        // ── Splash ───────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ───────────────────────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Login / Cadastro ─────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Home (Contém as Abas: Treino, Ranking e Perfil) ──────────────
        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}