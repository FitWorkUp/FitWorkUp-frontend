package com.fitworkup.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "fitworkup_theme")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val darkThemeEnabled: Flow<Boolean> = context.themeDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[DARK_THEME_ENABLED] ?: false }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.themeDataStore.edit { it[DARK_THEME_ENABLED] = enabled }
    }

    private companion object {
        val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
    }
}
