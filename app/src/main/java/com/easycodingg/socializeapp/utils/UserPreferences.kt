package com.easycodingg.socializeapp.utils

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    application: Application
) {
    private val Context.dataStore by preferencesDataStore(SOCIALIZE_DATA_STORE)
    private val applicationContext = application.applicationContext

    val authToken: Flow<String?> = applicationContext.dataStore.data.map { preferences ->
        preferences[KEY_AUTH_TOKEN]
    }

    suspend fun saveAuthToken(token: String) {
        applicationContext.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
        }
    }

    companion object {
        private const val SOCIALIZE_DATA_STORE = "socialize_data_store"
        private val KEY_AUTH_TOKEN = stringPreferencesKey("key_auth_token")
    }
}