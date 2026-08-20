package com.fitworkup.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.fitworkup.app.ui.screens.profile.ConfigScreen
import com.fitworkup.app.ui.screens.profile.FriendProfileScreen
import com.fitworkup.app.ui.screens.home.HomeScreen
import com.fitworkup.app.ui.screens.login.OnboardingScreen
import com.fitworkup.app.ui.screens.splash.SplashScreen
import com.fitworkup.app.ui.screens.workout.WorkoutScreen
import com.fitworkup.app.ui.screens.workout.group.GroupLobbyScreen
import com.fitworkup.app.ui.screens.home.components.WorkoutSetupAction
import com.fitworkup.app.ui.screens.login.LoginScreen
import com.fitworkup.app.ui.screens.login.PasswordRecoveryScreen
import com.fitworkup.app.data.session.SessionManager

object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val PASSWORD_RECOVERY = "password-recovery"
    const val HOME        = "home"
    const val WORKOUT     = "workout/{goalMeters}/{groupSessionId}"
    const val GROUP_LOBBY = "group-lobby/{mode}/{value}/{goalMeters}/{friendsOnly}"
    const val CONFIG      = "config"
    const val FRIEND_PROFILE = "friend-profile/{userId}"

    fun friendProfile(userId: String) = "friend-profile/$userId"
    fun workout(goalKm: Double?, groupSessionId: Long? = null): String {
        val goalMeters = goalKm
            ?.takeIf { it.isFinite() && it > 0.0 }
            ?.times(1_000.0)
            ?.toInt()
            ?: 0
        return "workout/$goalMeters/${groupSessionId ?: 0L}"
    }

    fun createGroup(roomName: String, goalKm: Double?, friendsOnly: Boolean): String =
        groupLobby("create", roomName, goalKm, friendsOnly)

    fun joinGroup(code: String): String =
        groupLobby("join", code, null, true)

    private fun groupLobby(
        mode: String,
        value: String,
        goalKm: Double?,
        friendsOnly: Boolean
    ): String {
        val goalMeters = goalKm
            ?.takeIf { it.isFinite() && it > 0.0 }
            ?.times(1_000.0)
            ?.toInt()
            ?: 0
        return "group-lobby/$mode/${Uri.encode(value)}/$goalMeters/$friendsOnly"
    }
}

@Composable
fun NavGraph(sessionManager: SessionManager) {
    val navController = rememberNavController()

    LaunchedEffect(sessionManager, navController) {
        sessionManager.sessionExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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
                onStartWorkoutClick = { action ->
                    when (action) {
                        is WorkoutSetupAction.StartSolo -> {
                            navController.navigate(Routes.workout(action.goalKm))
                        }
                        is WorkoutSetupAction.CreateGroup -> {
                            navController.navigate(
                                Routes.createGroup(
                                    roomName = action.roomName,
                                    goalKm = action.goalKm,
                                    friendsOnly = action.friendsOnly
                                )
                            )
                        }
                        is WorkoutSetupAction.JoinGroup -> {
                            navController.navigate(Routes.joinGroup(action.code))
                        }
                    }
                },
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
        composable(
            route = Routes.GROUP_LOBBY,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("value") { type = NavType.StringType },
                navArgument("goalMeters") { type = NavType.IntType },
                navArgument("friendsOnly") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode").orEmpty()
            val value = Uri.decode(backStackEntry.arguments?.getString("value").orEmpty())
            val goalMeters = backStackEntry.arguments?.getInt("goalMeters") ?: 0
            val friendsOnly = backStackEntry.arguments?.getBoolean("friendsOnly") ?: true
            GroupLobbyScreen(
                mode = mode,
                value = value,
                initialGoalKm = goalMeters.takeIf { it > 0 }?.div(1_000.0),
                friendsOnly = friendsOnly,
                onBack = { navController.popBackStack() },
                onWorkoutStarted = { goalKm, groupSessionId ->
                    navController.navigate(Routes.workout(goalKm, groupSessionId))
                }
            )
        }

        composable(
            route = Routes.WORKOUT,
            arguments = listOf(
                navArgument("goalMeters") { type = NavType.IntType },
                navArgument("groupSessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val goalMeters = backStackEntry.arguments?.getInt("goalMeters") ?: 0
            val groupSessionId = backStackEntry.arguments
                ?.getLong("groupSessionId")
                ?.takeIf { it > 0L }
            WorkoutScreen(
                distanceGoalKm = goalMeters.takeIf { it > 0 }?.div(1_000.0),
                groupSessionId = groupSessionId,
                onWorkoutFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
