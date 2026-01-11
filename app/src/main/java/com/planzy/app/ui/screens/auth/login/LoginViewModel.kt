package com.planzy.app.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.LoginUseCase
import com.planzy.app.domain.usecase.auth.ResendVerificationEmailUseCase
import com.planzy.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.planzy.app.domain.usecase.auth.UpdatePasswordUseCase
import com.planzy.app.ui.screens.auth.BaseAuthViewModel
import kotlinx.coroutines.delay
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
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val authRepository: AuthRepository,
    resourceProvider: ResourceProvider,
    cooldownManager: CooldownManager
) : BaseAuthViewModel(resourceProvider, cooldownManager) {

    private val _success = MutableStateFlow(false)
    override val success: StateFlow<Boolean> = _success

    private val _fieldErrors = MutableStateFlow(LoginFieldError())
    val fieldErrors: StateFlow<LoginFieldError> = _fieldErrors

    private val _showResendVerification = MutableStateFlow(false)
    val showResendVerification: StateFlow<Boolean> = _showResendVerification

    private val _forgotPasswordLoading = MutableStateFlow(false)
    val forgotPasswordLoading: StateFlow<Boolean> = _forgotPasswordLoading

    private val _forgotPasswordSuccess = MutableStateFlow(false)
    val forgotPasswordSuccess: StateFlow<Boolean> = _forgotPasswordSuccess

    private val _forgotPasswordMessage = MutableStateFlow<String?>(null)
    val forgotPasswordMessage: StateFlow<String?> = _forgotPasswordMessage

    private val _isResetPasswordMode = MutableStateFlow(false)
    val isResetPasswordMode: StateFlow<Boolean> = _isResetPasswordMode

    private val _resetPasswordLoading = MutableStateFlow(false)
    val resetPasswordLoading: StateFlow<Boolean> = _resetPasswordLoading

    private val _newPasswordError = MutableStateFlow<String?>(null)
    val newPasswordError: StateFlow<String?> = _newPasswordError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError

    private val _justResetPassword = MutableStateFlow(false)
    val justResetPassword: StateFlow<Boolean> = _justResetPassword

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

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _forgotPasswordLoading.value = true
            _forgotPasswordSuccess.value = false
            _forgotPasswordMessage.value = null

            val emailExists = authRepository.checkEmailExistsInAuth(email)

            if (emailExists.isSuccess && emailExists.getOrNull() == true) {
                val result = sendPasswordResetEmailUseCase(email)

                _forgotPasswordLoading.value = false
                if (result.isSuccess) {
                    _forgotPasswordSuccess.value = true
                    _forgotPasswordMessage.value = result.getOrNull()
                    startResendCooldown()
                } else {
                    _forgotPasswordSuccess.value = false
                    _error.value = result.exceptionOrNull()?.message
                }
            } else {
                _forgotPasswordLoading.value = false
                _forgotPasswordSuccess.value = false
                _error.value = resourceProvider.getString(R.string.error_email_not_found)
            }
        }
    }

    fun clearForgotPassword() {
        _forgotPasswordLoading.value = false
        _forgotPasswordSuccess.value = false
        _forgotPasswordMessage.value = null
    }

    fun enableResetPasswordMode() {
        _isResetPasswordMode.value = true
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _resetPasswordLoading.value = true
            _error.value = null
            _success.value = false
            _successMessage.value = null

            if (newPassword != confirmPassword) {
                _error.value = resourceProvider.getString(R.string.error_passwords_dont_match)
                _resetPasswordLoading.value = false
                return@launch
            }

            if (!isValidPassword(newPassword)) {
                _error.value = resourceProvider.getString(R.string.error_password_invalid)
                _resetPasswordLoading.value = false
                return@launch
            }

            val result = updatePasswordUseCase(newPassword)

            _resetPasswordLoading.value = false
            if (result.isSuccess) {
                _justResetPassword.value = true
                _success.value = true

                viewModelScope.launch {
                    delay(100)
                    _justResetPassword.value = false
                }
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun validateNewPassword(password: String) {
        val error = if (password.isNotEmpty() && !isValidPassword(password)) {
            resourceProvider.getString(R.string.error_password_invalid)
        } else null

        _newPasswordError.value = error
    }

    fun validateConfirmPassword(newPassword: String, confirmPassword: String) {
        val error = if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
            resourceProvider.getString(R.string.error_passwords_dont_match)
        } else null

        _confirmPasswordError.value = error
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
                    SendPasswordResetEmailUseCase(authRepository, resourceProvider),
                    UpdatePasswordUseCase(authRepository, resourceProvider),
                    authRepository,
                    resourceProvider,
                    cooldownManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}