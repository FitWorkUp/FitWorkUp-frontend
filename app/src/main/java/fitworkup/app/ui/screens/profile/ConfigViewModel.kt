package com.fitworkup.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitworkup.app.data.preferences.ReminderPreferences
import com.fitworkup.app.data.preferences.ThemePreferences
import com.fitworkup.app.data.preferences.WeeklyGoalPreferences
import com.fitworkup.app.domain.repository.AuthRepository
import com.fitworkup.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConfigUiState(
    val weeklyGoalEnabled: Boolean = true,
    val weeklyGoalDays: Int = 3,
    val activityReminderEnabled: Boolean = false,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0,
    val reminderDays: Set<Int> = setOf(1, 3, 5),
    val returnReminderEnabled: Boolean = false,
    val returnAfterDays: Int = 3,
    val darkThemeEnabled: Boolean = false
)

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weeklyGoalPreferences: WeeklyGoalPreferences,
    private val reminderPreferences: ReminderPreferences,
    private val themePreferences: ThemePreferences,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    val uiState: StateFlow<ConfigUiState> = combine(
        weeklyGoalPreferences.settings,
        reminderPreferences.settings,
        themePreferences.darkThemeEnabled
    ) { weeklyGoal, reminders, darkTheme ->
        ConfigUiState(
            weeklyGoalEnabled = weeklyGoal.enabled,
            weeklyGoalDays = weeklyGoal.targetDays,
            activityReminderEnabled = reminders.activityReminderEnabled,
            reminderHour = reminders.reminderHour,
            reminderMinute = reminders.reminderMinute,
            reminderDays = reminders.reminderDays,
            returnReminderEnabled = reminders.returnReminderEnabled,
            returnAfterDays = reminders.returnAfterDays,
            darkThemeEnabled = darkTheme
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConfigUiState())

    init {
        viewModelScope.launch {
            reminderPreferences.settings.collect(reminderScheduler::update)
        }
    }

    fun updateWeeklyGoalDays(days: Int) {
        viewModelScope.launch {
            weeklyGoalPreferences.update(uiState.value.weeklyGoalEnabled, days)
        }
    }

    fun setActivityReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { reminderPreferences.setActivityReminderEnabled(enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch { reminderPreferences.setReminderTime(hour, minute) }
    }

    fun toggleReminderDay(day: Int) {
        val currentDays = uiState.value.reminderDays
        val newDays = if (day in currentDays) currentDays - day else currentDays + day
        if (newDays.isEmpty()) return
        viewModelScope.launch { reminderPreferences.setReminderDays(newDays) }
    }

    fun setReturnReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { reminderPreferences.setReturnReminderEnabled(enabled) }
    }

    fun setReturnAfterDays(days: Int) {
        viewModelScope.launch { reminderPreferences.setReturnAfterDays(days) }
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch { themePreferences.setDarkThemeEnabled(enabled) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loggedOut.value = true
        }
    }
}
