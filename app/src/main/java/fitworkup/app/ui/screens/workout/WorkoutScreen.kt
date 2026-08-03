package com.fitworkup.app.ui.screens.workout

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.ui.screens.workout.components.WorkoutMetricsSection
import com.fitworkup.app.ui.viewmodel.WorkoutViewModel

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onWorkoutFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
    }

    fun hasAllPermissions(ctx: Context): Boolean {
        return requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    var hasPermissions by remember { mutableStateOf(hasAllPermissions(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap.values.all { it }
        hasPermissions = granted
        if (!granted) {
            showPermissionDialog = true
        }
    }

    // 🚀 DISPARO DO TREINO: Inicia a contagem quando tiver permissão e ainda não estiver rastreando
    LaunchedEffect(hasPermissions) {
        if (hasPermissions && !uiState.isTracking) {
            viewModel.startWorkout(context)
        }
    }

    // Monitora permissões e erros vindos do ViewModel
    LaunchedEffect(uiState.permissionNeeded, uiState.errorMessage) {
        if (uiState.permissionNeeded) {
            showPermissionDialog = true
        } else if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    if (showPermissionDialog || !hasPermissions) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permissões de Treino Necessárias") },
            text = {
                Text(
                    "Para registrar o treino e validar seus passos, o FitWorkUp precisa das permissões de Localização (GPS) e Reconhecimento de Atividade Física.\n\nPor favor, conceda as permissões para continuar."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!hasPermissions) {
                            permissionLauncher.launch(requiredPermissions)
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            showPermissionDialog = false
                        }
                    }
                ) {
                    Text("Conceder / Configurações")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPermissionDialog = false
                        onWorkoutFinished()
                    }
                ) {
                    Text("Cancelar e Sair")
                }
            }
        )
    }

    // 📱 statusBarsPadding() impede a sobreposição com a câmera notch / barra de status
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
            // Seção de Cronômetro e Métricas com suporte a Tema Escuro
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
                    Text("Finalizar Treino")
                }
            }
        }
    }
}