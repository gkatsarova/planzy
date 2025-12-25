package com.planzy.app.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.LoginUseCase
import com.planzy.app.domain.usecase.ResendVerificationEmailUseCase
import com.planzy.app.ui.screens.auth.BaseAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginFieldError(
    val emailError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val resendVerificationEmailUseCase: ResendVerificationEmailUseCase,
    resourceProvider: ResourceProvider,
    cooldownManager: CooldownManager
) : BaseAuthViewModel(resourceProvider, cooldownManager) {

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _fieldErrors = MutableStateFlow(LoginFieldError())
    val fieldErrors: StateFlow<LoginFieldError> = _fieldErrors

    private val _showResendVerification = MutableStateFlow(false)
    val showResendVerification: StateFlow<Boolean> = _showResendVerification

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success.value = false
            _successMessage.value = null
            _showResendVerification.value = false

            val result = loginUseCase(email, password)

            _loading.value = false
            if (result.isSuccess) {
                _success.value = true
                _successMessage.value = result.getOrNull()
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                _error.value = errorMessage

                if (errorMessage?.contains(
                        resourceProvider.getString(R.string.error_email_not_verified),
                        ignoreCase = true
                    ) == true ||
                    errorMessage?.contains("verify", ignoreCase = true) == true ||
                    errorMessage?.contains("verification", ignoreCase = true) == true
                ) {
                    _showResendVerification.value = true
                }
            }
        }
    }

    fun validateEmail(email: String) {
        val error = if (email.isNotEmpty() && !isValidEmail(email)) {
            resourceProvider.getString(R.string.error_email_invalid)
        } else null

        _fieldErrors.value = _fieldErrors.value.copy(emailError = error)
    }

    fun validatePassword(password: String) {
        val error = if (password.isNotEmpty() && !isValidPassword(password)) {
            resourceProvider.getString(R.string.error_password_invalid)
        } else null

        _fieldErrors.value = _fieldErrors.value.copy(passwordError = error)
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = resendVerificationEmailUseCase(email)

            _loading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
                startResendCooldown()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val resourceProvider: ResourceProvider,
        private val cooldownManager: CooldownManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(
                    LoginUseCase(authRepository, resourceProvider),
                    ResendVerificationEmailUseCase(authRepository, resourceProvider),
                    resourceProvider,
                    cooldownManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}