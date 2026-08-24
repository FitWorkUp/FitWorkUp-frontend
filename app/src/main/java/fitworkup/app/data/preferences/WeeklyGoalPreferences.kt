package com.fitworkup.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.weeklyGoalDataStore by preferencesDataStore(name = "fitworkup_weekly_goal")

data class WeeklyGoalSettings(
    val enabled: Boolean = true,
    val targetDays: Int = 3
)

@Singleton
class WeeklyGoalPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val settings: Flow<WeeklyGoalSettings> = context.weeklyGoalDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            WeeklyGoalSettings(
                enabled = preferences[WEEKLY_GOAL_ENABLED] ?: true,
                targetDays = (preferences[WEEKLY_GOAL_DAYS] ?: 3).coerceIn(1, 7)
            )
        }

    suspend fun update(enabled: Boolean, targetDays: Int) {
        context.weeklyGoalDataStore.edit { preferences ->
            preferences[WEEKLY_GOAL_ENABLED] = enabled
            preferences[WEEKLY_GOAL_DAYS] = targetDays.coerceIn(1, 7)
        }
    }

    private companion object {
        val WEEKLY_GOAL_ENABLED = booleanPreferencesKey("weekly_goal_enabled")
        val WEEKLY_GOAL_DAYS = intPreferencesKey("weekly_goal_days")
    }
}
