package com.planzy.app.data.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class RecoverySessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "recovery_session_prefs",
        Context.MODE_PRIVATE
    )

    fun saveRecoverySession(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        }
    }

    fun getRecoverySession(): RecoverySession? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)

        if (accessToken == null || refreshToken == null) {
            return null
        }

        val oneHourInMillis = 60 * 60 * 1000
        if (System.currentTimeMillis() - timestamp > oneHourInMillis) {
            clearRecoverySession()
            return null
        }

        return RecoverySession(accessToken, refreshToken)
    }

    fun clearRecoverySession() {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "recovery_access_token"
        private const val KEY_REFRESH_TOKEN = "recovery_refresh_token"
        private const val KEY_TIMESTAMP = "recovery_timestamp"
    }
}

data class RecoverySession(
    val accessToken: String,
    val refreshToken: String
)