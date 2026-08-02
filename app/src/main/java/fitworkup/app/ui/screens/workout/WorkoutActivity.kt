package com.fitworkup.ui.workout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fitworkup.app.ui.screens.workout.WorkoutScreen
import com.fitworkup.app.ui.theme.FitWorkUpTheme

class WorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitWorkUpTheme {
                WorkoutScreen(
                    onWorkoutFinished = {
                        finish()
                    }
                )
            }
        }
    }
}