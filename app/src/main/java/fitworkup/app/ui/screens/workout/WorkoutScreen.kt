package com.fitworkup.app.ui.screens.workout

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.service.WorkoutSensorService
import com.fitworkup.app.ui.screens.workout.components.WorkoutMetricsSection
import com.fitworkup.app.ui.viewmodel.WorkoutViewModel

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onWorkoutFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val serviceConnection = remember(viewModel) {
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                binder: IBinder?
            ) {
                val workoutBinder =
                    binder as? WorkoutSensorService.LocalBinder

                workoutBinder
                    ?.getService()
                    ?.let(viewModel::onServiceConnected)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                viewModel.onServiceDisconnected()
            }
        }
    }

    DisposableEffect(context, serviceConnection) {
        val intent = Intent(
            context,
            WorkoutSensorService::class.java
        )

        val wasBound = context.bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        onDispose {
            if (wasBound) {
                runCatching {
                    context.unbindService(serviceConnection)
                }
            }

            viewModel.onServiceDisconnected()
        }
    }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }.toTypedArray()
    }

    fun hasAllPermissions(ctx: Context): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(ctx, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    var hasPermissions by remember {
        mutableStateOf(hasAllPermissions(context))
    }

    var showPermissionDialog by remember {
        mutableStateOf(!hasPermissions)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermissions = hasAllPermissions(context)
        showPermissionDialog = !hasPermissions
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions && !uiState.isTracking) {
            viewModel.startWorkout(context)
        }
    }

    LaunchedEffect(uiState.permissionNeeded, uiState.errorMessage) {
        when {
            uiState.permissionNeeded -> {
                showPermissionDialog = true
            }

            uiState.errorMessage != null -> {
                Toast.makeText(
                    context,
                    uiState.errorMessage,
                    Toast.LENGTH_LONG
                ).show()

                viewModel.clearErrorMessage()
            }
        }
    }

    if (showPermissionDialog || !hasPermissions) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Permissões de treino necessárias")
            },
            text = {
                Text(
                    "Para registrar o treino e validar seus passos, " +
                            "o FitWorkUp precisa das permissões de localização " +
                            "e reconhecimento de atividade física."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionLauncher.launch(requiredPermissions)
                    }
                ) {
                    Text("Conceder permissões")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPermissionDialog = false
                        onWorkoutFinished()
                    }
                ) {
                    Text("Cancelar e sair")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorkoutMetricsSection(uiState = uiState)

            Button(
                onClick = {
                    if (hasPermissions) {
                        viewModel.finishWorkout(context)
                        onWorkoutFinished()
                    } else {
                        showPermissionDialog = true
                    }
                },
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Finalizar treino")
                }
            }
        }
    }
}
