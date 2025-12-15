package org.apps.composestoryapp.remote

import android.preference.Preference
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.apps.composestoryapp.model.LoginResult

class SessionManager(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val NAME_KEY = stringPreferencesKey("name")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    suspend fun saveSession(session: LoginResult) {
        dataStore.edit {
            it[TOKEN_KEY] = session.token
            it[NAME_KEY] = session.name
            it[USER_ID_KEY] = session.userId
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    val sessionFlow: Flow<LoginResult?> =
        dataStore.data.map {prefs ->
            val token = prefs[TOKEN_KEY]
            val name = prefs[NAME_KEY]
            val userId = prefs[USER_ID_KEY]

            if (token != null && name != null && userId != null) {
                LoginResult(
                    userId = userId,
                    name = name,
                    token = token
                )
            } else {
                null
            }
        }
}