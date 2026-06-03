package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository

class UpdatePasswordUseCase(
    private val authRepository: AuthRepository
) {
    private val TAG = UpdatePasswordUseCase::class.java.simpleName

    suspend operator fun invoke(newPassword: String): Result<Unit> {
        return try {
            val result = authRepository.updatePassword(newPassword)

            if (result.isSuccess) {
                Log.i(TAG, "Password updated successfully")
                Result.success(Unit)
            } else {
                val exception = result.exceptionOrNull()
                    ?: AppException(AppError.ERROR_UPDATE_PASSWORD)
                Log.e(TAG, "Failed to update password: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password: ${e.message}", e)
            val finalException = e as? AppException ?: AppException(AppError.ERROR_UPDATE_PASSWORD)
            Result.failure(finalException)
        }
    }
}