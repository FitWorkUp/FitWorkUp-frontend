package com.fitworkup.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitworkup.app.ui.screens.profile.ConfigScreen
import com.fitworkup.app.ui.screens.profile.FriendProfileScreen
import com.fitworkup.app.ui.screens.home.HomeScreen
import com.fitworkup.app.ui.screens.login.OnboardingScreen
import com.fitworkup.app.ui.screens.splash.SplashScreen
import com.fitworkup.app.ui.screens.workout.WorkoutScreen
import com.fitworkup.app.ui.screens.login.LoginScreen
import com.fitworkup.app.ui.screens.login.PasswordRecoveryScreen

object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val PASSWORD_RECOVERY = "password-recovery"
    const val HOME        = "home"
    const val WORKOUT     = "workout"
    const val CONFIG      = "config"
    const val FRIEND_PROFILE = "friend-profile/{userId}"

    fun friendProfile(userId: String) = "friend-profile/$userId"
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
            OnboardingScreen(onFinishOnboarding = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(Routes.PASSWORD_RECOVERY) }
            )
        }

        composable(Routes.PASSWORD_RECOVERY) {
            PasswordRecoveryScreen(
                onBack = { navController.popBackStack() },
                onPasswordChanged = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PASSWORD_RECOVERY) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onStartWorkoutClick = { navController.navigate(Routes.WORKOUT) },
                onSettingsClick = { navController.navigate(Routes.CONFIG) },
                onFriendProfileClick = { userId ->
                    navController.navigate(Routes.friendProfile(userId))
                }
            )
        }

        composable(Routes.FRIEND_PROFILE) {
            FriendProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CONFIG) {
            ConfigScreen(
                onBackClick = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Rota do Treino com Anti-Fraude Integrado
        composable(Routes.WORKOUT) {
            WorkoutScreen(
                onWorkoutFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}
