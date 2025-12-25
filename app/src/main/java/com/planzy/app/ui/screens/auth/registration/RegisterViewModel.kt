package com.planzy.app.ui.screens.auth.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.usecase.CheckEmailAvailabilityUseCase
import com.planzy.app.domain.usecase.CheckUsernameAvailabilityUseCase
import com.planzy.app.domain.usecase.RegisterUserUseCase
import com.planzy.app.domain.usecase.ResendVerificationEmailUseCase
import com.planzy.app.ui.screens.auth.BaseAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _fieldErrors = MutableStateFlow(FieldError())
    val fieldErrors: StateFlow<FieldError> = _fieldErrors

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success.value = false
            _successMessage.value = null

            val result = registerUserUseCase(email, password, username)

            _loading.value = false
            if (result.isSuccess) {
                _success.value = true
                val message = result.getOrNull() ?: ""
                _successMessage.value = if (message.contains(
                        resourceProvider.getString(R.string.verification_email),
                        ignoreCase = true
                    )
                ) {
                    resourceProvider.getString(R.string.success_verification_email_sent)
                } else {
                    message
                }

                if (message.contains(resourceProvider.getString(R.string.verification_email), ignoreCase = true)) {
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
                resourceProvider.getString(R.string.error_username_invalid)
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
                resourceProvider.getString(R.string.error_email_invalid)
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
        val error = if (password.isNotEmpty() && !isValidPassword(password)) {
            resourceProvider.getString(R.string.error_password_invalid)
        } else null

        _fieldErrors.value = _fieldErrors.value.copy(passwordError = error)
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
        private val userRepository: UserRepository,
        private val resourceProvider: ResourceProvider,
        private val cooldownManager: CooldownManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(
                    RegisterUserUseCase(authRepository, resourceProvider),
                    CheckUsernameAvailabilityUseCase(userRepository, resourceProvider),
                    CheckEmailAvailabilityUseCase(authRepository, resourceProvider),
                    ResendVerificationEmailUseCase(authRepository, resourceProvider),
                    resourceProvider,
                    cooldownManager
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}