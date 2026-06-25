package com.fitworkup.app.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGoalBottomSheet(
    onDismissRequest: () -> Unit,
    onStartWorkout: (kmMeta: Double?, isGroup: Boolean, groupCode: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val suggestions = listOf("2", "5", "10")
    var selectedSuggestion by remember { mutableStateOf<String?>(null) }
    var customKm by remember { mutableStateOf("") }
    var isFreeStyle by remember { mutableStateOf(true) }

    var isGroupMode by remember { mutableStateOf(false) }
    var groupCodeField by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configurar sua Corrida",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Seleção de Modo (Solo vs Grupo)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    onClick = { isGroupMode = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isGroupMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏃‍♂️ Solo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Card(
                    onClick = { isGroupMode = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGroupMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥 Em Grupo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            if (isGroupMode) {
                OutlinedTextField(
                    value = groupCodeField,
                    onValueChange = { groupCodeField = it },
                    label = { Text("Código da Sala (Opcional)") },
                    placeholder = { Text("Ex: FTW-982") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
            }

            // Estilo Livre
            Card(
                onClick = {
                    isFreeStyle = true
                    selectedSuggestion = null
                    customKm = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFreeStyle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = isFreeStyle, onClick = {
                        isFreeStyle = true
                        selectedSuggestion = null
                        customKm = ""
                    })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Estilo Livre", fontWeight = FontWeight.Bold)
                        Text("Corra sem limite de distância", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ou estabeleça uma meta de distância",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { km ->
                    val isSelected = selectedSuggestion == km && !isFreeStyle
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            isFreeStyle = false
                            selectedSuggestion = km
                            customKm = ""
                        },
                        label = { Text("$km km") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customKm,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        customKm = newValue
                        if (newValue.isNotEmpty()) {
                            isFreeStyle = false
                            selectedSuggestion = null
                        }
                    }
                },
                label = { Text("Digitar quilometragem manual") },
                suffix = { Text("km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalKm = when {
                        isFreeStyle -> null
                        customKm.isNotEmpty() -> customKm.toDoubleOrNull()
                        else -> selectedSuggestion?.toDoubleOrNull()
                    }
                    onStartWorkout(finalKm, isGroupMode, groupCodeField)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isFreeStyle || selectedSuggestion != null || customKm.isNotEmpty()
            ) {
                val modeLabel = if (isGroupMode) "EM GRUPO" else "SOLO"
                val goalLabel = when {
                    isFreeStyle -> "ESTILO LIVRE"
                    customKm.isNotEmpty() -> "$customKm KM"
                    else -> "$selectedSuggestion KM"
                }
                Text("INICIAR ATIVIDADE $modeLabel ($goalLabel)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}