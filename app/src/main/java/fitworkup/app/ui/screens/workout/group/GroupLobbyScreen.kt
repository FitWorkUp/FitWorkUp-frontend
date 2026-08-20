package com.fitworkup.app.ui.screens.workout.group

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitworkup.app.data.remote.dto.GroupParticipantDto
import com.fitworkup.app.data.remote.dto.GroupSessionDto
import com.fitworkup.app.ui.components.RemoteContentError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupLobbyScreen(
    mode: String,
    value: String,
    initialGoalKm: Double?,
    friendsOnly: Boolean,
    onBack: () -> Unit,
    onWorkoutStarted: (goalKm: Double?, groupSessionId: Long) -> Unit,
    viewModel: GroupLobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var navigationHandled by remember { mutableStateOf(false) }

    LaunchedEffect(mode, value, initialGoalKm, friendsOnly) {
        viewModel.initialize(mode, value, initialGoalKm, friendsOnly)
    }
    LaunchedEffect(uiState.session?.status) {
        val session = uiState.session
        if (session?.status == "ACTIVE" && !navigationHandled) {
            navigationHandled = true
            onWorkoutStarted(session.targetDistanceKm, session.id)
        }
    }
    LaunchedEffect(uiState.leftLobby) {
        if (uiState.leftLobby) onBack()
    }

    val requestExit = {
        if (uiState.session == null) onBack() else viewModel.leaveLobby()
    }
    BackHandler(onBack = requestExit)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lobby da atividade") },
                navigationIcon = {
                    IconButton(onClick = requestExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.session == null -> RemoteContentError(
                    title = uiState.errorMessage ?: "Não foi possível abrir a sala",
                    onRetry = {
                        viewModel.retry(mode, value, initialGoalKm, friendsOnly)
                    }
                )
                else -> LobbyContent(
                    session = requireNotNull(uiState.session),
                    isProcessing = uiState.isProcessing,
                    errorMessage = uiState.errorMessage,
                    onRefresh = viewModel::refresh,
                    onReadyChanged = viewModel::setReady,
                    onStart = viewModel::startWorkout,
                    onLeave = viewModel::leaveLobby
                )
            }
        }
    }
}

@Composable
private fun LobbyContent(
    session: GroupSessionDto,
    isProcessing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onReadyChanged: (Boolean) -> Unit,
    onStart: () -> Unit,
    onLeave: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val everybodyReady = session.participants.size >= 2 && session.participants.all { it.ready }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                session.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                session.targetDistanceKm?.let { "Meta: ${formatGoal(it)}" }
                    ?: "Estilo livre",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Código da sala", style = MaterialTheme.typography.labelMedium)
                        Text(
                            session.code,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(session.code)) }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar código")
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val shareIntent = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Entre na atividade ${session.name} no FitWorkUp com o código ${session.code}."
                            )
                        },
                        "Compartilhar código da sala"
                    )
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Compartilhar código")
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Participantes (${session.participants.size}/${session.maxParticipants})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                }
            }
        }

        items(session.participants, key = { it.id }) { participant ->
            ParticipantRow(participant)
        }

        if (errorMessage != null) {
            item {
                Text(
                    errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            if (session.currentUserHost) {
                Button(
                    onClick = onStart,
                    enabled = everybodyReady && !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("INICIAR ATIVIDADE", fontWeight = FontWeight.Bold)
                    }
                }
                if (!everybodyReady) {
                    Text(
                        if (session.participants.size < 2) {
                            "Aguarde pelo menos mais um participante."
                        } else {
                            "Aguarde todos marcarem que estão prontos."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Button(
                    onClick = { onReadyChanged(!session.currentUserReady) },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = if (session.currentUserReady) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(
                        if (session.currentUserReady) "PRONTO ✓" else "ESTOU PRONTO",
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "O treino abrirá quando o anfitrião iniciar.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            TextButton(
                onClick = onLeave,
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sair da sala", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ParticipantRow(participant: GroupParticipantDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    buildString {
                        append(participant.username)
                        if (participant.currentUser) append(" (você)")
                    },
                    fontWeight = FontWeight.SemiBold
                )
                if (participant.host) {
                    Text(
                        "Anfitrião",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = if (participant.ready) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.HourglassEmpty
                },
                contentDescription = null,
                tint = if (participant.ready) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (participant.ready) "Pronto" else "Aguardando",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatGoal(goalKm: Double): String =
    if (goalKm < 1.0) "${(goalKm * 1_000).toInt()} m" else "${goalKm} km"
