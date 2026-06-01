package com.fitworkup.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitworkup.app.ui.screens.splash.SplashScreen
import com.fitworkup.app.ui.screens.onboarding.OnboardingScreen
import com.fitworkup.app.ui.screens.login.LoginScreen
import com.fitworkup.app.ui.screens.home.HomeScreen

// ─── Rotas ──────────────────────────────────────────────────────────────────
object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val HOME        = "home"
    // Adicione aqui as próximas rotas:
    // const val RANKING  = "ranking"
    // const val LOJA     = "loja"
    // const val PERFIL   = "perfil/{userId}"
}

/**
 * NavGraph
 *
 * Ponto central de navegação do FitWorkUp.
 * Coloque este Composable dentro do FitWorkUpTheme na MainActivity.
 *
 * Exemplo na MainActivity:
 *   setContent {
 *       FitWorkUpTheme {
 *           NavGraph()
 *       }
 *   }
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
                        // Remove splash da back stack — botão voltar não volta para ela
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
                        // Remove login da back stack — usuário autenticado não volta para login
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Home (placeholder) ───────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen()
        }

        // Adicione as próximas telas abaixo seguindo o mesmo padrão:
        //
        // composable(Routes.RANKING) {
        //     RankingScreen(onNavigateBack = { navController.popBackStack() })
        // }
    }
}
