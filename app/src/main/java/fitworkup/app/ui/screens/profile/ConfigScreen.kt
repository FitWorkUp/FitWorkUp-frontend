package com.fitworkup.app.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showTimePicker by remember { mutableStateOf(false) }
    var permissionTarget by remember { mutableStateOf<NotificationPermissionTarget?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        when (permissionTarget) {
            NotificationPermissionTarget.ACTIVITY -> viewModel.setActivityReminderEnabled(granted)
            NotificationPermissionTarget.RETURN -> viewModel.setReturnReminderEnabled(granted)
            null -> Unit
        }
        permissionTarget = null
    }

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLoggedOut()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader("META SEMANAL")
            SettingCard(
                icon = Icons.Outlined.CalendarMonth,
                title = "Dias de atividade por semana",
                subtitle = "Treinos no mesmo dia contam apenas uma vez"
            ) {
                Text(
                    text = "${uiState.weeklyGoalDays} ${if (uiState.weeklyGoalDays == 1) "dia" else "dias"}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = uiState.weeklyGoalDays.toFloat(),
                    onValueChange = { viewModel.updateWeeklyGoalDays(it.roundToInt()) },
                    valueRange = 1f..7f,
                    steps = 5
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1", style = MaterialTheme.typography.labelSmall)
                    Text("7", style = MaterialTheme.typography.labelSmall)
                }
            }

            SectionHeader("LEMBRETES")
            SwitchSettingCard(
                icon = Icons.Outlined.NotificationsActive,
                title = "Lembrete de atividade",
                subtitle = if (uiState.activityReminderEnabled) {
                    "${formatTime(uiState.reminderHour, uiState.reminderMinute)} nos dias selecionados"
                } else {
                    "Receba um aviso nos dias planejados"
                },
                checked = uiState.activityReminderEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        viewModel.setActivityReminderEnabled(false)
                    } else if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionTarget = NotificationPermissionTarget.ACTIVITY
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setActivityReminderEnabled(true)
                    }
                }
            )

            if (uiState.activityReminderEnabled) {
                SettingCard(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Horário",
                    subtitle = "O Android pode entregar o lembrete alguns minutos depois"
                ) {
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text(formatTime(uiState.reminderHour, uiState.reminderMinute))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Dias da semana", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    DaySelector(
                        selectedDays = uiState.reminderDays,
                        onDayClick = viewModel::toggleReminderDay
                    )
                }
            }

            SwitchSettingCard(
                icon = Icons.Outlined.RestartAlt,
                title = "Lembrete de retomada",
                subtitle = "Avisar após um período sem atividade concluída",
                checked = uiState.returnReminderEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        viewModel.setReturnReminderEnabled(false)
                    } else if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionTarget = NotificationPermissionTarget.RETURN
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setReturnReminderEnabled(true)
                    }
                }
            )

            if (uiState.returnReminderEnabled) {
                SettingCard(
                    icon = Icons.Outlined.RestartAlt,
                    title = "Avisar depois de",
                    subtitle = "Será enviado no máximo um lembrete por dia"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 5, 7).forEach { days ->
                            FilterChip(
                                selected = uiState.returnAfterDays == days,
                                onClick = { viewModel.setReturnAfterDays(days) },
                                label = { Text("$days d") }
                            )
                        }
                    }
                }
            }

            SectionHeader("APARÊNCIA")
            SwitchSettingCard(
                icon = Icons.Outlined.DarkMode,
                title = "Tema escuro",
                subtitle = "Aplicar em todas as telas do aplicativo",
                checked = uiState.darkThemeEnabled,
                onCheckedChange = viewModel::setDarkThemeEnabled
            )

            SectionHeader("CONTA")
            OutlinedButton(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Sair da conta")
            }

            SectionHeader("COMPARTILHAR")
            MenuItem(
                icon = Icons.Outlined.Share,
                title = "Compartilhe com um amigo",
                subtitle = "Disponível quando o aplicativo tiver um link público",
                enabled = false,
                onClick = {}
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Horário do lembrete") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DaySelector(selectedDays: Set<Int>, onDayClick: (Int) -> Unit) {
    val days = listOf(1 to "Seg", 2 to "Ter", 3 to "Qua", 4 to "Qui", 5 to "Sex", 6 to "Sáb", 7 to "Dom")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.take(4).forEach { (value, label) ->
                FilterChip(
                    selected = value in selectedDays,
                    onClick = { onDayClick(value) },
                    label = { Text(label) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.drop(4).forEach { (value, label) ->
                FilterChip(
                    selected = value in selectedDays,
                    onClick = { onDayClick(value) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingIcon(icon)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SwitchSettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIcon(icon)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 4.dp),
        letterSpacing = 1.sp
    )
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { SettingIcon(icon) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.5f),
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f)
        )
    )
}

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

private enum class NotificationPermissionTarget {
    ACTIVITY,
    RETURN
}
