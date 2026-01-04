package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository

class RegisterUserUseCase(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) {
    private val TAG = RegisterUserUseCase::class.java.simpleName

    suspend operator fun invoke(
        email: String,
        password: String,
        username: String): Result<String> {
        return try {
            Log.d(TAG, "Registration started")

            val authResult = authRepository.signUp(email, password, username)
            if (authResult.isFailure) {
                return Result.failure(
                    Exception(authResult.exceptionOrNull()?.message ?: resourceProvider.getString(R.string.error_registration_failed))
                )
            }

            val authUser = authResult.getOrNull()
            if (authUser == null) {
                Log.e(TAG, "Auth user is null after successful signup")
                return Result.failure(Exception(resourceProvider.getString(R.string.error_registration_failed)))
            }

            Log.i(TAG, "Auth user created with ID: ${authUser.id}")
            Log.i(TAG, "Verification email sent to: $email")

            Result.success(resourceProvider.getString(R.string.success_verification_email_sent))
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            Result.failure(Exception(e.message ?: resourceProvider.getString(R.string.error_registration_failed)))
        }
    }
}