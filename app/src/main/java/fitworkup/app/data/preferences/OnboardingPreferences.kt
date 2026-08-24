package com.fitworkup.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "fitworkup_onboarding")

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun needsOnboarding(): Boolean = context.onboardingDataStore.data
        .map { preferences ->
            preferences[ONBOARDING_VERSION] ?: 0
        }
        .first() < CURRENT_ONBOARDING_VERSION

    suspend fun markCurrentVersionCompleted() {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_VERSION] = CURRENT_ONBOARDING_VERSION
        }
    }

    private companion object {
        const val CURRENT_ONBOARDING_VERSION = 1
        val ONBOARDING_VERSION = intPreferencesKey("onboarding_version")
    }
}
