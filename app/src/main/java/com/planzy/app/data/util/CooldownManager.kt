package com.planzy.app.data.util

import android.content.Context
import android.content.SharedPreferences

class CooldownManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_cooldown_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_COOLDOWN_END_TIME = "resend_cooldown_end_time"
    }

    fun setCooldownEndTime(endTimeMillis: Long) {
        prefs.edit().putLong(KEY_COOLDOWN_END_TIME, endTimeMillis).apply()
    }

    fun getRemainingCooldownSeconds(): Int {
        val endTime = prefs.getLong(KEY_COOLDOWN_END_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val remainingMillis = endTime - currentTime

        return if (remainingMillis > 0) {
            (remainingMillis / 1000).toInt()
        } else {
            0
        }
    }

    fun clearCooldown() {
        prefs.edit().remove(KEY_COOLDOWN_END_TIME).apply()
    }
}