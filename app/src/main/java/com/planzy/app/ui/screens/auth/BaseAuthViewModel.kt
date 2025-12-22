package com.planzy.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.util.ResourceProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel(
    protected val resourceProvider: ResourceProvider
) : ViewModel() {

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
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

    protected fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    protected fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }

    protected fun startResendCooldown(seconds: Int = 60) {
        _canResendEmail.value = false
        _resendCooldownSeconds.value = seconds

        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            repeat(seconds) {
                delay(1_000)
                _resendCooldownSeconds.value = seconds - (it + 1)
            }
            _canResendEmail.value = true
            _resendCooldownSeconds.value = 0
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