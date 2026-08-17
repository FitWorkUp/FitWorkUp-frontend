package com.fitworkup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.data.connectivity.ConnectivityStatus
import com.fitworkup.app.data.connectivity.NetworkMonitor
import com.fitworkup.app.navigation.NavGraph
import com.fitworkup.app.ui.components.OfflineBanner
import com.fitworkup.app.ui.theme.FitWorkUpTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            val connectivityStatus by networkMonitor.status.collectAsStateWithLifecycle(
                initialValue = ConnectivityStatus.CHECKING
            )

            FitWorkUpTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph()

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
}
