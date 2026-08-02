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

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onWorkoutFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permissões necessárias para rastreamento de passos e localização (GPS)
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (!allGranted) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            WorkoutMetricsSection(uiState = uiState)

            Button(
                onClick = { viewModel.finishWorkout("CAMINHADA") },
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