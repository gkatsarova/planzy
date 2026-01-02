package com.planzy.app.domain.usecase

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository

class SendPasswordResetEmailUseCase(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) {
    private val TAG = SendPasswordResetEmailUseCase::class.java.simpleName

    suspend operator fun invoke(email: String): Result<String> {
        return try {
            Log.d(TAG, "Sending password reset email to: $email")

            val result = authRepository.sendPasswordResetEmail(email)

            if (result.isSuccess) {
                Log.i(TAG, "Password reset email sent successfully")
                Result.success(resourceProvider.getString(R.string.password_reset_email_sent))
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                    ?: resourceProvider.getString(R.string.error_password_reset_email_failed)
                Log.e(TAG, "Failed to send password reset email: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending password reset email: ${e.message}", e)
            Result.failure(
                Exception(e.message ?: resourceProvider.getString(R.string.error_password_reset_email_failed))
            )
        }
    }
}