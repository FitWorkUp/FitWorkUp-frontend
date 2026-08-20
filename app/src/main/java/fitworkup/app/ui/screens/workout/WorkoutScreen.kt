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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.fitworkup.app.ui.screens.workout.components.WorkoutMapView
import com.fitworkup.app.ui.screens.workout.components.WorkoutMetricsSection
import com.google.android.gms.maps.model.LatLng


@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun WorkoutScreen(

    viewModel: WorkoutViewModel = hiltViewModel(),

    distanceGoalKm: Double? = null,

    groupSessionId: Long? = null,

    onWorkoutFinished: () -> Unit

) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val currentViewModel by rememberUpdatedState(viewModel)

    val scrollState = rememberScrollState()

    LaunchedEffect(distanceGoalKm, groupSessionId) {
        viewModel.configureWorkout(distanceGoalKm, groupSessionId)
    }



// Conversão otimizada de android.location.Location para com.google.android.gms.maps.model.LatLng

    val currentLocationLatLng = remember(uiState.currentLocation) {

        uiState.currentLocation?.let { LatLng(it.latitude, it.longitude) }

    }

    val pathPointsLatLng = remember(uiState.pathPoints) {

        uiState.pathPoints.map { LatLng(it.latitude, it.longitude) }

    }



// Lista dinâmica de permissões por versão do Android

    val requiredPermissions = remember {

        val permissions = mutableListOf(

            Manifest.permission.ACCESS_FINE_LOCATION,

            Manifest.permission.ACCESS_COARSE_LOCATION

        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            permissions.add(Manifest.permission.POST_NOTIFICATIONS)

        }

        permissions.toTypedArray()

    }



    var showPermissionDialog by remember { mutableStateOf(false) }

    var isBound by remember { mutableStateOf(false) }



    fun hasAllPermissions(ctx: Context): Boolean {

        return requiredPermissions.all { perm ->

            ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED

        }

    }



// Instância memorizada da conexão de serviço

    val connection = remember {

        object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

                val binder = service as? WorkoutSensorService.LocalBinder

                binder?.getService()?.let { sensorService ->

                    currentViewModel.onServiceConnected(sensorService)

                }

            }



            override fun onServiceDisconnected(name: ComponentName?) {}

        }

    }



// Função para iniciar e vincular ao Foreground Service

    fun startAndBindService() {

        try {

            val startIntent = Intent(context, WorkoutSensorService::class.java).apply {

                action = WorkoutSensorService.ACTION_START

            }

            ContextCompat.startForegroundService(context, startIntent)



            if (!isBound) {

                val serviceIntent = Intent(context, WorkoutSensorService::class.java)

                isBound = context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

            }

        } catch (e: Exception) {

            e.printStackTrace()

        }

    }



// Lançador de permissões

    val permissionLauncher = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.RequestMultiplePermissions()

    ) { permissionsMap ->

        val allGranted = permissionsMap.values.all { it }

        if (allGranted) {

            startAndBindService()

        } else {

            showPermissionDialog = true

        }

    }



// Verificação inicial de permissões

    LaunchedEffect(Unit) {

        if (hasAllPermissions(context)) {

            startAndBindService()

        } else {

            permissionLauncher.launch(requiredPermissions)

        }

    }



// Gerenciamento de desvinculação do serviço

    DisposableEffect(context) {

        onDispose {

            if (isBound) {

                try {

                    context.unbindService(connection)

                    isBound = false

                } catch (e: Exception) {

                    e.printStackTrace()

                }

            }

        }

    }



// Monitoramento do encerramento do treino

    LaunchedEffect(uiState.submissionSuccess, uiState.errorMessage) {

        if (uiState.submissionSuccess == true) {

            val stopIntent = Intent(context, WorkoutSensorService::class.java).apply {

                action = WorkoutSensorService.ACTION_STOP

            }

            context.stopService(stopIntent)

            Toast.makeText(context, "Treino registrado com sucesso!", Toast.LENGTH_SHORT).show()

            onWorkoutFinished()

        } else if (uiState.errorMessage != null) {

            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()

        }

    }



// Diálogo de permissões

    if (showPermissionDialog) {

        AlertDialog(

            onDismissRequest = { },

            title = { Text("Permissões Necessárias") },

            text = {

                Text(

                    "O FitWorkUp precisa de acesso ao seu GPS (Localização), Notificações e aos Sensores de Atividade Física para registrar seu percurso e validar o treino."

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

                .verticalScroll(scrollState)

                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

// 🗺️ 1. MAPA DO PERCURSO EM TEMPO REAL (Com adaptação LatLng)

            WorkoutMapView(

                currentLocation = currentLocationLatLng,

                pathPoints = pathPointsLatLng,

                hasPermissions = hasAllPermissions(context),

                modifier = Modifier.fillMaxWidth()

            )



// 📊 2. MÉTRICAS TELEMÉTRICAS E ANTI-FRAUDE

            WorkoutMetricsSection(

                uiState = uiState,

                modifier = Modifier.fillMaxWidth()

            )



            Spacer(modifier = Modifier.height(8.dp))



// 🏁 3. BOTÃO DE FINALIZAR TREINO

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

