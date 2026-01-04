package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) {
    private val TAG = LoginUseCase::class.java.simpleName

    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<String> {
        return try {
            Log.d(TAG, "Login started for email: $email")

            val authResult = authRepository.signIn(email, password)

            if (authResult.isFailure) {
                val errorMessage = authResult.exceptionOrNull()?.message
                    ?: resourceProvider.getString(R.string.error_login_failed)
                return Result.failure(Exception(errorMessage))
            }

            val authUser = authResult.getOrNull()
            if (authUser == null) {
                Log.e(TAG, "Auth user is null after successful login")
                return Result.failure(
                    Exception(resourceProvider.getString(R.string.error_login_failed))
                )
            }

            if (authUser.emailConfirmedAt == null) {
                Log.w(TAG, "Email not verified for user: ${authUser.email}")
                return Result.failure(
                    Exception(resourceProvider.getString(R.string.error_email_not_verified))
                )
            }

            Log.i(TAG, "Login successful for user: ${authUser.id}")
            return Result.success(resourceProvider.getString(R.string.success_login))

        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            Result.failure(
                Exception(e.message ?: resourceProvider.getString(R.string.error_login_failed))
            )
        }
    }
}