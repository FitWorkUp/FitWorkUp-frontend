package com.fitworkup.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.sessionDataStore by preferencesDataStore(name = "fitworkup_session")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val tokenFlow: Flow<String?> = context.sessionDataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    suspend fun saveToken(token: String) {
        context.sessionDataStore.edit { it[ACCESS_TOKEN] = token }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.remove(ACCESS_TOKEN) }
    }

    suspend fun hasToken(): Boolean = !tokenFlow.first().isNullOrBlank()

    fun getTokenBlocking(): String? = runBlocking { tokenFlow.first() }

    fun clearBlocking() = runBlocking { clear() }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }
}
