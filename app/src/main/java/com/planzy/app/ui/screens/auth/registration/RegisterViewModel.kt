package com.planzy.app.ui.screens.auth.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.usecase.auth.CheckEmailAvailabilityUseCase
import com.planzy.app.domain.usecase.auth.CheckUsernameAvailabilityUseCase
import com.planzy.app.domain.usecase.auth.RegisterUserUseCase
import com.planzy.app.domain.usecase.auth.ResendVerificationEmailUseCase
import com.planzy.app.ui.screens.auth.BaseAuthViewModel
import kotlinx.coroutines.launch

data class FieldError(
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class RegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val checkUsernameAvailabilityUseCase: CheckUsernameAvailabilityUseCase,
    private val checkEmailAvailabilityUseCase: CheckEmailAvailabilityUseCase,
    private val resendVerificationEmailUseCase: ResendVerificationEmailUseCase,
    resourceProvider: ResourceProvider,
    cooldownManager: CooldownManager
) : BaseAuthViewModel(resourceProvider, cooldownManager) {

    companion object {
        private val USERNAME_REGEX = Regex("^[a-z0-9._]{3,20}$")
    }

    override var success by mutableStateOf(false)

    var fieldErrors by mutableStateOf(FieldError())
        private set

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            loading = true
            error = null
            success = false
            successMessage = null

            val result = registerUserUseCase(email, password, username)

            loading = false
            if (result.isSuccess) {
                success = true
                successMessage = resourceProvider.getString(R.string.success_verification_email_sent)
                startResendCooldown()
            } else {
                val appError = (result.exceptionOrNull() as? AppException)?.error ?: AppError.ERROR_REGISTRATION_FAILED
                error = when (appError) {
                    AppError.ERROR_NO_INTERNET -> resourceProvider.getString(R.string.error_no_internet)
                    AppError.ERROR_EMAIL_EXISTS -> resourceProvider.getString(R.string.error_email_exists)
                    else -> resourceProvider.getString(R.string.error_registration_failed)
                }
            }
        }
    }

    fun validateUsername(username: String) {
        viewModelScope.launch {
            val formatError = if (username.isNotEmpty() && !USERNAME_REGEX.matches(username)) {
                resourceProvider.getString(R.string.error_username_invalid)
            } else null

            val availabilityError = if (formatError == null && username.isNotEmpty()) {
                checkUsernameAvailability(username)
            } else null

            fieldErrors = fieldErrors.copy(
                usernameError = formatError ?: availabilityError
            )
        }
    }

    fun validateEmail(email: String) {
        viewModelScope.launch {
            val formatError = if (email.isNotEmpty() && !isValidEmail(email)) {
                resourceProvider.getString(R.string.error_email_invalid)
            } else null

            val availabilityError = if (formatError == null && email.isNotEmpty()) {
                checkEmailAvailability(email)
            } else null

            fieldErrors = fieldErrors.copy(
                emailError = formatError ?: availabilityError
            )
        }
    }

    fun validatePassword(password: String) {
        val error = if (password.isNotEmpty() && !isValidPassword(password)) {
            resourceProvider.getString(R.string.error_password_invalid)
        } else null

        fieldErrors = fieldErrors.copy(passwordError = error)
    }

    private suspend fun checkUsernameAvailability(username: String): String? {
        val result = checkUsernameAvailabilityUseCase(username)
        return if (result.isSuccess) {
            val isAvailable = result.getOrNull() ?: true
            if (isAvailable) null else resourceProvider.getString(R.string.error_username_exists)
        } else null
    }

    private suspend fun checkEmailAvailability(email: String): String? {
        val result = checkEmailAvailabilityUseCase(email)
        return if (result.isSuccess) {
            val isAvailable = result.getOrNull() ?: true
            if (isAvailable) null else resourceProvider.getString(R.string.error_email_exists)
        } else null
    }

    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            loading = true
            error = null

            val result = resendVerificationEmailUseCase(email)

            loading = false
            if (result.isSuccess) {
                successMessage = resourceProvider.getString(R.string.success_resend_verification_email)
                startResendCooldown()
            } else {
                val appError = (result.exceptionOrNull() as? AppException)?.error ?: AppError.ERROR_VERIFICATION_EMAIL_RESEND
                error = when (appError) {
                    AppError.ERROR_NO_INTERNET -> resourceProvider.getString(R.string.error_no_internet)
                    else -> resourceProvider.getString(R.string.error_verification_email_resend)
                }
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val resourceProvider: ResourceProvider,
        private val cooldownManager: CooldownManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(
                    RegisterUserUseCase(authRepository),
                    CheckUsernameAvailabilityUseCase(userRepository),
                    CheckEmailAvailabilityUseCase(authRepository),
                    ResendVerificationEmailUseCase(authRepository),
                    resourceProvider,
                    cooldownManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}