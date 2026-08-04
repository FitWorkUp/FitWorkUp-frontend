package com.fitworkup.app.ui.screens.workout

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.service.WorkoutSensorService
import com.fitworkup.app.ui.screens.workout.components.WorkoutMetricsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onWorkoutFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    fun hasAllPermissions(ctx: Context): Boolean {
        return requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    DisposableEffect(Unit) {
        val serviceIntent = Intent(context, WorkoutSensorService::class.java)

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? WorkoutSensorService.LocalBinder
                binder?.getService()?.let { sensorService ->
                    viewModel.onServiceConnected(sensorService)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        if (hasAllPermissions(context)) {
            val startIntent = Intent(context, WorkoutSensorService::class.java).apply {
                action = WorkoutSensorService.ACTION_START
            }
            ContextCompat.startForegroundService(context, startIntent)
            context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }

        onDispose {
            try {
                context.unbindService(connection)
            } catch (e: Exception) {
                // Serviço desvinculado com sucesso
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            val startIntent = Intent(context, WorkoutSensorService::class.java).apply {
                action = WorkoutSensorService.ACTION_START
            }
            ContextCompat.startForegroundService(context, startIntent)
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasAllPermissions(context)) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    LaunchedEffect(uiState.submissionSuccess, uiState.errorMessage) {
        if (uiState.submissionSuccess == true) {
            val stopIntent = Intent(context, WorkoutSensorService::class.java).apply {
                action = WorkoutSensorService.ACTION_STOP
            }
            context.startService(stopIntent) // Envia a action STOP explicitamente
            Toast.makeText(context, "Treino registrado com sucesso!", Toast.LENGTH_SHORT).show()
            onWorkoutFinished()
        } else if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permissões Necessárias") },
            text = {
                Text(
                    "O FitWorkUp precisa de acesso ao seu GPS (Localização) e aos Sensores de Atividade Física para contagem de passos e validação anti-fraude em tempo real."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Configurações")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        onWorkoutFinished()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Treino em Andamento",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorkoutMetricsSection(
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { viewModel.finishWorkout("CAMINHADA") },
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Finalizar Treino",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}