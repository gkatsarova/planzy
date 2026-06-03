package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository

class SendPasswordResetEmailUseCase(
    private val authRepository: AuthRepository
) {
    private val TAG = SendPasswordResetEmailUseCase::class.java.simpleName

    suspend operator fun invoke(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "Sending password reset email to: $email")

            val result = authRepository.sendPasswordResetEmail(email)

            if (result.isSuccess) {
                Log.i(TAG, "Password reset email sent successfully")
                Result.success(Unit)
            } else {
                val exception = result.exceptionOrNull()
                    ?: AppException(AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED)
                Log.e(TAG, "Failed to send password reset email: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending password reset email: ${e.message}", e)
            val finalException = e as? AppException ?: AppException(AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED)
            Result.failure(finalException)
        }
    }
}