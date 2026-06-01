package com.planzy.app.ui.screens.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.manager.ProfilePictureManager
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.usecase.auth.LoginUseCase
import com.planzy.app.domain.usecase.auth.ResendVerificationEmailUseCase
import com.planzy.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.planzy.app.domain.usecase.auth.UpdatePasswordUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.screens.auth.BaseAuthViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
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
    private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase,
    private val authRepository: AuthRepository,
    resourceProvider: ResourceProvider,
    cooldownManager: CooldownManager
) : BaseAuthViewModel(resourceProvider, cooldownManager) {

    override var success by mutableStateOf(false)

    var fieldErrors by mutableStateOf(LoginFieldError())
        private set

    var showResendVerification by mutableStateOf(false)
        private set

    var forgotPasswordLoading by mutableStateOf(false)
        private set

    var forgotPasswordSuccess by mutableStateOf(false)
        private set

    var forgotPasswordMessage by mutableStateOf<String?>(null)
        private set

    var isResetPasswordMode by mutableStateOf(false)
        private set

    var resetPasswordLoading by mutableStateOf(false)
        private set

    var newPasswordError by mutableStateOf<String?>(null)
        private set

    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var justResetPassword by mutableStateOf(false)
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loading = true
            error = null
            success = false
            successMessage = null
            showResendVerification = false

            val result = loginUseCase(email, password)

            loading = false
            if (result.isSuccess) {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    getUserByAuthIdUseCase(userId).onSuccess { user ->
                        ProfilePictureManager.updateUrl(user?.profilePictureUrl)
                    }
                }
                success = true
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                error = errorMessage

                if (errorMessage?.contains(
                        resourceProvider.getString(R.string.error_email_not_verified),
                        ignoreCase = true
                    ) == true ||
                    errorMessage?.contains("verify", ignoreCase = true) == true ||
                    errorMessage?.contains("verification", ignoreCase = true) == true
                ) {
                    showResendVerification = true
                }
            }
        }
    }

    fun validateEmail(email: String) {
        val error = if (email.isNotEmpty() && !isValidEmail(email)) {
            resourceProvider.getString(R.string.error_email_invalid)
        } else null

        fieldErrors = fieldErrors.copy(emailError = error)
    }

    fun validatePassword(password: String) {
        val error = if (password.isNotEmpty() && !isValidPassword(password)) {
            resourceProvider.getString(R.string.error_password_invalid)
        } else null

        fieldErrors = fieldErrors.copy(passwordError = error)
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            loading = true
            error = null

            val result = resendVerificationEmailUseCase(email)

            loading = false
            if (result.isSuccess) {
                successMessage = result.getOrNull()
                startResendCooldown()
            } else {
                error = result.exceptionOrNull()?.message
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            forgotPasswordLoading = true
            forgotPasswordSuccess = false
            forgotPasswordMessage = null

            val emailExists = authRepository.checkEmailExistsInAuth(email)

            if (emailExists.isSuccess && emailExists.getOrNull() == true) {
                val result = sendPasswordResetEmailUseCase(email)

                forgotPasswordLoading = false
                if (result.isSuccess) {
                    forgotPasswordSuccess = true
                    forgotPasswordMessage = result.getOrNull()
                    startResendCooldown()
                } else {
                    forgotPasswordSuccess = false
                    error = result.exceptionOrNull()?.message
                }
            } else {
                forgotPasswordLoading = false
                forgotPasswordSuccess = false
                error = resourceProvider.getString(R.string.error_email_not_found)
            }
        }
    }

    fun clearForgotPassword() {
        forgotPasswordLoading = false
        forgotPasswordSuccess = false
        forgotPasswordMessage = null
    }

    fun enableResetPasswordMode() {
        isResetPasswordMode = true
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            resetPasswordLoading = true
            error = null
            success = false
            successMessage = null

            if (newPassword != confirmPassword) {
                error = resourceProvider.getString(R.string.error_passwords_dont_match)
                resetPasswordLoading = false
                return@launch
            }

            if (!isValidPassword(newPassword)) {
                error = resourceProvider.getString(R.string.error_password_invalid)
                resetPasswordLoading = false
                return@launch
            }

            val result = updatePasswordUseCase(newPassword)

            resetPasswordLoading = false
            if (result.isSuccess) {
                justResetPassword = true
                success = true

                viewModelScope.launch {
                    delay(100)
                    justResetPassword = false
                }
            } else {
                error = result.exceptionOrNull()?.message
            }
        }
    }

    fun validateNewPassword(password: String) {
        val error = if (password.isNotEmpty() && !isValidPassword(password)) {
            resourceProvider.getString(R.string.error_password_invalid)
        } else null

        newPasswordError = error
    }

    fun validateConfirmPassword(newPassword: String, confirmPassword: String) {
        val error = if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
            resourceProvider.getString(R.string.error_passwords_dont_match)
        } else null

        confirmPasswordError = error
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
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
                    GetUserByAuthIdUseCase(userRepository),
                    authRepository,
                    resourceProvider,
                    cooldownManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}