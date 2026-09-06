package com.fitworkup.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.data.connectivity.ConnectivityStatus
import com.fitworkup.app.data.connectivity.NetworkMonitor
import com.fitworkup.app.data.preferences.ThemePreferences
import com.fitworkup.app.data.session.SessionManager
import com.fitworkup.app.navigation.NavGraph
import com.fitworkup.app.navigation.WorkoutNotificationRequest
import com.fitworkup.app.service.WorkoutSensorService
import com.fitworkup.app.ui.components.OfflineBanner
import com.fitworkup.app.ui.theme.FitWorkUpTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var workoutNotificationRequest by mutableStateOf<WorkoutNotificationRequest?>(null)

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            val connectivityStatus by networkMonitor.status.collectAsStateWithLifecycle(
                initialValue = ConnectivityStatus.CHECKING
            )
            val darkThemeEnabled by themePreferences.darkThemeEnabled.collectAsStateWithLifecycle(
                initialValue = false
            )

            FitWorkUpTheme(darkTheme = darkThemeEnabled) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        sessionManager = sessionManager,
                        workoutNotificationRequest = workoutNotificationRequest
                    )

                    AnimatedVisibility(
                        visible = connectivityStatus == ConnectivityStatus.OFFLINE,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        OfflineBanner()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != WorkoutSensorService.ACTION_OPEN_WORKOUT) return

        workoutNotificationRequest = WorkoutNotificationRequest(
            requestId = System.nanoTime(),
            goalKm = intent.getDoubleExtra(WorkoutSensorService.EXTRA_GOAL_KM, Double.NaN)
                .takeIf { it.isFinite() && it > 0.0 },
            groupSessionId = intent.getLongExtra(
                WorkoutSensorService.EXTRA_GROUP_SESSION_ID,
                0L
            ).takeIf { it > 0L }
        )
    }
}
