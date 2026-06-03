package com.planzy.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel(
    protected val resourceProvider: ResourceProvider,
    protected val cooldownManager: CooldownManager
) : ViewModel() {

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
        const val DEFAULT_RESEND_COOLDOWN_SECONDS = 60
    }

    var loading by mutableStateOf(false)
        protected set

    var error by mutableStateOf<String?>(null)
        protected set

    var successMessage by mutableStateOf<String?>(null)
        protected set

    var canResendEmail by mutableStateOf(true)
        protected set

    var resendCooldownSeconds by mutableIntStateOf(0)
        protected set

    open var success by mutableStateOf(false)
        protected set

    private var resendCooldownJob: Job? = null

    init {
        checkExistingCooldown()
    }

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }

    private fun checkExistingCooldown() {
        val remainingSeconds = cooldownManager.getRemainingCooldownSeconds()
        if (remainingSeconds > 0) {
            startResendCooldown(remainingSeconds)
        }
    }

    fun startResendCooldown(seconds: Int = DEFAULT_RESEND_COOLDOWN_SECONDS) {
        canResendEmail = false
        resendCooldownSeconds = seconds

        val endTimeMillis = System.currentTimeMillis() + (seconds * 1000L)
        cooldownManager.setCooldownEndTime(endTimeMillis)

        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            repeat(seconds) {
                delay(1_000)
                resendCooldownSeconds = seconds - (it + 1)
            }
            canResendEmail = true
            resendCooldownSeconds = 0
            cooldownManager.clearCooldown()
        }
    }

    fun clearError() {
        error = null
    }

    fun clearSuccess() {
        successMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        resendCooldownJob?.cancel()
    }

    fun setAuthError(message: String) {
        error = message
        success = false
        successMessage = null
    }
}