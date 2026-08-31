package com.fitworkup.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "fitworkup_reminders")

data class ReminderSettings(
    val activityReminderEnabled: Boolean = false,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0,
    /** Dias no padrão java.time.DayOfWeek: segunda = 1, domingo = 7. */
    val reminderDays: Set<Int> = setOf(1, 3, 5),
    val returnReminderEnabled: Boolean = false,
    val returnAfterDays: Int = 3,
    val lastReturnReminderEpochDay: Long? = null
)

@Singleton
class ReminderPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val settings: Flow<ReminderSettings> = context.reminderDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            ReminderSettings(
                activityReminderEnabled = preferences[ACTIVITY_REMINDER_ENABLED] ?: false,
                reminderHour = (preferences[REMINDER_HOUR] ?: 18).coerceIn(0, 23),
                reminderMinute = (preferences[REMINDER_MINUTE] ?: 0).coerceIn(0, 59),
                reminderDays = preferences[REMINDER_DAYS]
                    ?.mapNotNull(String::toIntOrNull)
                    ?.filter { it in 1..7 }
                    ?.toSet()
                    ?.takeIf(Set<Int>::isNotEmpty)
                    ?: setOf(1, 3, 5),
                returnReminderEnabled = preferences[RETURN_REMINDER_ENABLED] ?: false,
                returnAfterDays = (preferences[RETURN_AFTER_DAYS] ?: 3)
                    .takeIf { it in RETURN_DAY_OPTIONS }
                    ?: 3,
                lastReturnReminderEpochDay = preferences[LAST_RETURN_REMINDER_EPOCH_DAY]
            )
        }

    suspend fun current(): ReminderSettings = settings.first()

    suspend fun setActivityReminderEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { it[ACTIVITY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit {
            it[REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setReminderDays(days: Set<Int>) {
        if (days.isEmpty()) return
        context.reminderDataStore.edit {
            it[REMINDER_DAYS] = days.filter { day -> day in 1..7 }.map(Int::toString).toSet()
        }
    }

    suspend fun setReturnReminderEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { it[RETURN_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReturnAfterDays(days: Int) {
        if (days !in RETURN_DAY_OPTIONS) return
        context.reminderDataStore.edit { it[RETURN_AFTER_DAYS] = days }
    }

    suspend fun markReturnReminderSent(epochDay: Long) {
        context.reminderDataStore.edit { it[LAST_RETURN_REMINDER_EPOCH_DAY] = epochDay }
    }

    companion object {
        val RETURN_DAY_OPTIONS = setOf(2, 3, 5, 7)

        private val ACTIVITY_REMINDER_ENABLED = booleanPreferencesKey("activity_reminder_enabled")
        private val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        private val REMINDER_DAYS = stringSetPreferencesKey("reminder_days")
        private val RETURN_REMINDER_ENABLED = booleanPreferencesKey("return_reminder_enabled")
        private val RETURN_AFTER_DAYS = intPreferencesKey("return_after_days")
        private val LAST_RETURN_REMINDER_EPOCH_DAY = longPreferencesKey("last_return_reminder_epoch_day")
    }
}
