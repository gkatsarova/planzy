package com.planzy.app.ui.screens.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FieldError(
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

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

            val result = repo.signUp(email, password, username)

            _loading.value = false
            if (result.isSuccess) {
                _success.value = true
                _successMessage.value = "Verification email is sent. Please check $email."

                if (result.getOrNull()?.contains("Verification email", ignoreCase = true) == true) {
                    startResendCooldown()
                }

            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun validateUsername(username: String) {
        viewModelScope.launch {
            val formatError = repo.getUsernameValidationError(username)
            val existsError = if (formatError == null && username.isNotEmpty()) {
                repo.getUsernameExistsError(username)
            } else null

            _fieldErrors.value = _fieldErrors.value.copy(
                usernameError = formatError ?: existsError
            )
        }
    }

    fun validateEmail(email: String) {
        viewModelScope.launch {
            val formatError = repo.getEmailValidationError(email)
            val existsError = if (formatError == null && email.isNotEmpty()) {
                repo.getEmailExistsError(email)
            } else null

            _fieldErrors.value = _fieldErrors.value.copy(
                emailError = formatError ?: existsError
            )
        }
    }

    fun validatePassword(password: String){
        val error = repo.getPasswordValidationError(password)
        _fieldErrors.value = _fieldErrors.value.copy(passwordError = error)

    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repo.resendVerificationEmail(email)

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

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}