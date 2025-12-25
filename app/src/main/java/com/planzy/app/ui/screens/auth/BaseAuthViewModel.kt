package com.planzy.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    protected val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    protected val _canResendEmail = MutableStateFlow(true)
    val canResendEmail: StateFlow<Boolean> = _canResendEmail

    protected val _resendCooldownSeconds = MutableStateFlow(0)
    val resendCooldownSeconds: StateFlow<Int> = _resendCooldownSeconds

    private var resendCooldownJob: Job? = null

    init {
        checkExistingCooldown()
    }

    protected fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    protected fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }

    private fun checkExistingCooldown() {
        val remainingSeconds = cooldownManager.getRemainingCooldownSeconds()
        if (remainingSeconds > 0) {
            startResendCooldown(remainingSeconds)
        }
    }

    protected fun startResendCooldown(seconds: Int = DEFAULT_RESEND_COOLDOWN_SECONDS) {
        _canResendEmail.value = false
        _resendCooldownSeconds.value = seconds

        val endTimeMillis = System.currentTimeMillis() + (seconds * 1000L)
        cooldownManager.setCooldownEndTime(endTimeMillis)

        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            repeat(seconds) {
                delay(1_000)
                _resendCooldownSeconds.value = seconds - (it + 1)
            }
            _canResendEmail.value = true
            _resendCooldownSeconds.value = 0
            cooldownManager.clearCooldown()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        resendCooldownJob?.cancel()
    }
}