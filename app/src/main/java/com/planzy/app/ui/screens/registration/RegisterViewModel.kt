package com.planzy.app.ui.screens.registration

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.domain.model.Messages
import com.planzy.app.domain.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FieldError(
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class RegisterViewModel(private val authService: AuthService) : ViewModel() {

    companion object {
        private val USERNAME_REGEX = Regex("^[a-z0-9._]{3,20}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
    }

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _fieldErrors = MutableStateFlow(FieldError())
    val fieldErrors: StateFlow<FieldError> = _fieldErrors

    private val _canResendEmail = MutableStateFlow(true)
    val canResendEmail: StateFlow<Boolean> = _canResendEmail

    private val _resendCooldownSeconds = MutableStateFlow(0)
    val resendCooldownSeconds: StateFlow<Int> = _resendCooldownSeconds

    private var resendCooldownJob: kotlinx.coroutines.Job? = null

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success.value = false
            _successMessage.value = null

            val result = authService.registerUser(email, password, username)

            _loading.value = false
            if (result.isSuccess) {
                _success.value = true
                val message = result.getOrNull()!!
                _successMessage.value = if (message.contains("Verification email", ignoreCase = true)) {
                    "Verification email is sent. Please check $email."
                } else {
                    message
                }

                if (message.contains("Verification email", ignoreCase = true)) {
                    startResendCooldown()
                }
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun validateUsername(username: String) {
        viewModelScope.launch {
            val formatError = if (username.isNotEmpty() && !USERNAME_REGEX.matches(username)) {
                Messages.ERROR_USERNAME_INVALID
            } else null

            val availabilityError = if (formatError == null && username.isNotEmpty()) {
                checkUsernameAvailability(username)
            } else null

            _fieldErrors.value = _fieldErrors.value.copy(
                usernameError = formatError ?: availabilityError
            )
        }
    }

    fun validateEmail(email: String) {
        viewModelScope.launch {
            val formatError = if (email.isNotEmpty() && !isValidEmail(email)) {
                Messages.ERROR_EMAIL_INVALID
            } else null

            val availabilityError = if (formatError == null && email.isNotEmpty()) {
                checkEmailAvailability(email)
            } else null

            _fieldErrors.value = _fieldErrors.value.copy(
                emailError = formatError ?: availabilityError
            )
        }
    }

    fun validatePassword(password: String) {
        val error = if (password.isNotEmpty() && !PASSWORD_REGEX.matches(password)) {
            Messages.ERROR_PASSWORD_INVALID
        } else null

        _fieldErrors.value = _fieldErrors.value.copy(passwordError = error)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private suspend fun checkUsernameAvailability(username: String): String? {
        val result = authService.checkUsernameAvailability(username)
        return if (result.isSuccess) {
            val isAvailable = result.getOrNull() ?: true
            if (isAvailable) null else Messages.ERROR_USERNAME_EXISTS
        } else null
    }

    private suspend fun checkEmailAvailability(email: String): String? {
        val result = authService.checkEmailAvailability(email)
        return if (result.isSuccess) {
            val isAvailable = result.getOrNull() ?: true
            if (isAvailable) null else Messages.ERROR_EMAIL_EXISTS
        } else null
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = authService.resendVerificationEmail(email)

            _loading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
                startResendCooldown()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    private fun startResendCooldown() {
        _canResendEmail.value = false
        _resendCooldownSeconds.value = 60

        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            repeat(60) {
                kotlinx.coroutines.delay(1_000)
                _resendCooldownSeconds.value = 60 - (it + 1)
            }
            _canResendEmail.value = true
            _resendCooldownSeconds.value = 0
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = false
        _successMessage.value = null
    }

    class Factory(private val authService: AuthService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(authService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}