package com.fitworkup.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitworkup.app.ui.screens.splash.SplashScreen
import com.fitworkup.app.ui.screens.login.OnboardingScreen
import com.fitworkup.app.ui.screens.login.LoginScreen
import com.fitworkup.app.ui.screens.home.HomeScreen
import com.fitworkup.app.ui.screens.profile.ConfigScreen
import com.fitworkup.app.ui.screens.workout.WorkoutScreen
object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val HOME        = "home"
    const val WORKOUT     = "Workout"
    const val CONFIG      = "config"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onNavigateToLogin = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(onNavigateToHome = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                onStartWorkoutClick = { navController.navigate(Routes.WORKOUT) },
                onSettingsClick = { navController.navigate(Routes.CONFIG) }
            )
        }

        composable(Routes.CONFIG) {
            ConfigScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.WORKOUT) {
            WorkoutScreen(
                onFinishWorkout = { navController.popBackStack() }
            )
        }
    }
}