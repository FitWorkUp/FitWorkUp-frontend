package com.fitworkup.app.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

sealed interface WorkoutSetupAction {
    data class StartSolo(val goalKm: Double?) : WorkoutSetupAction

    data class CreateGroup(
        val roomName: String,
        val goalKm: Double?,
        val friendsOnly: Boolean
    ) : WorkoutSetupAction

    data class JoinGroup(val code: String) : WorkoutSetupAction
}

private enum class GroupSetupMode { CREATE, JOIN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGoalBottomSheet(
    onDismissRequest: () -> Unit,
    onContinue: (WorkoutSetupAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isGroupMode by remember { mutableStateOf(false) }
    var groupSetupMode by remember { mutableStateOf(GroupSetupMode.CREATE) }
    var roomName by remember { mutableStateOf("") }
    var groupCode by remember { mutableStateOf("") }
    var friendsOnly by remember { mutableStateOf(true) }
    var selectedSuggestion by remember { mutableStateOf<String?>(null) }
    var customKm by remember { mutableStateOf("") }
    var isFreeStyle by remember { mutableStateOf(true) }

    val finalKm = when {
        isFreeStyle -> null
        customKm.isNotBlank() -> customKm.toDoubleOrNull()
        else -> selectedSuggestion?.toDoubleOrNull()
    }
    val normalizedCode = groupCode.trim().uppercase(Locale.ROOT)
    val isCodeValid = normalizedCode.matches(Regex("^FTW-[A-Z0-9]{4}$"))
    val canContinue = when {
        !isGroupMode -> isFreeStyle || finalKm != null
        groupSetupMode == GroupSetupMode.CREATE -> roomName.trim().length >= 3 &&
            (isFreeStyle || finalKm != null)
        else -> isCodeValid
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configurar sua corrida",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeCard(
                    label = "Solo",
                    selected = !isGroupMode,
                    modifier = Modifier.weight(1f),
                    onClick = { isGroupMode = false }
                )
                ModeCard(
                    label = "Em grupo",
                    selected = isGroupMode,
                    modifier = Modifier.weight(1f),
                    onClick = { isGroupMode = true }
                )
            }

            if (isGroupMode) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = groupSetupMode == GroupSetupMode.CREATE,
                        onClick = { groupSetupMode = GroupSetupMode.CREATE },
                        label = { Text("Criar sala") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = groupSetupMode == GroupSetupMode.JOIN,
                        onClick = { groupSetupMode = GroupSetupMode.JOIN },
                        label = { Text("Entrar com código") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                if (groupSetupMode == GroupSetupMode.CREATE) {
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it.take(40) },
                        label = { Text("Nome da sala") },
                        placeholder = { Text("Ex.: Corrida de domingo") },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sala apenas para amigos", fontWeight = FontWeight.Medium)
                            Text(
                                "Limite inicial: 5 participantes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = friendsOnly, onCheckedChange = { friendsOnly = it })
                    }
                } else {
                    OutlinedTextField(
                        value = groupCode,
                        onValueChange = {
                            groupCode = it.uppercase(Locale.ROOT).filter { char ->
                                char.isLetterOrDigit() || char == '-'
                            }.take(8)
                        },
                        label = { Text("Código da sala") },
                        placeholder = { Text("FTW-8K2P") },
                        supportingText = {
                            if (groupCode.isNotEmpty() && !isCodeValid) {
                                Text("Use o formato FTW-XXXX")
                            }
                        },
                        isError = groupCode.isNotEmpty() && !isCodeValid,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (!isGroupMode || groupSetupMode == GroupSetupMode.CREATE) {
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(18.dp))
                DistanceGoalSelector(
                    isFreeStyle = isFreeStyle,
                    selectedSuggestion = selectedSuggestion,
                    customKm = customKm,
                    onFreeStyleSelected = {
                        isFreeStyle = true
                        selectedSuggestion = null
                        customKm = ""
                    },
                    onSuggestionSelected = {
                        isFreeStyle = false
                        selectedSuggestion = it
                        customKm = ""
                    },
                    onCustomKmChanged = {
                        customKm = it
                        if (it.isNotEmpty()) {
                            isFreeStyle = false
                            selectedSuggestion = null
                        }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val action = when {
                        !isGroupMode -> WorkoutSetupAction.StartSolo(finalKm)
                        groupSetupMode == GroupSetupMode.CREATE -> WorkoutSetupAction.CreateGroup(
                            roomName = roomName.trim(),
                            goalKm = finalKm,
                            friendsOnly = friendsOnly
                        )
                        else -> WorkoutSetupAction.JoinGroup(normalizedCode)
                    }
                    onContinue(action)
                },
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when {
                        !isGroupMode -> "INICIAR ATIVIDADE"
                        groupSetupMode == GroupSetupMode.CREATE -> "CRIAR SALA"
                        else -> "ENTRAR NA SALA"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(14.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DistanceGoalSelector(
    isFreeStyle: Boolean,
    selectedSuggestion: String?,
    customKm: String,
    onFreeStyleSelected: () -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onCustomKmChanged: (String) -> Unit
) {
    Card(
        onClick = onFreeStyleSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFreeStyle) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isFreeStyle, onClick = onFreeStyleSelected)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Estilo livre", fontWeight = FontWeight.Bold)
                Text(
                    "Sem limite de distância",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        "Ou estabeleça uma meta de distância",
        modifier = Modifier.fillMaxWidth(),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("2", "5", "10").forEach { km ->
            FilterChip(
                selected = !isFreeStyle && selectedSuggestion == km,
                onClick = { onSuggestionSelected(km) },
                label = { Text("$km km") },
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = customKm,
        onValueChange = { value ->
            val normalized = value.replace(',', '.')
            if (normalized.isEmpty() || normalized.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) {
                onCustomKmChanged(normalized)
            }
        },
        label = { Text("Quilometragem manual") },
        suffix = { Text("km") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
