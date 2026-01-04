package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository

class UpdatePasswordUseCase(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) {
    private val TAG = UpdatePasswordUseCase::class.java.simpleName

    suspend operator fun invoke(newPassword: String): Result<String> {
        return try {
            val result = authRepository.updatePassword(newPassword)

            if (result.isSuccess) {
                Log.i(TAG, "Password updated successfully")
                Result.success(resourceProvider.getString(R.string.success_password_updated))
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                    ?: resourceProvider.getString(R.string.error_update_password)
                Log.e(TAG, "Failed to update password: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password: ${e.message}", e)
            Result.failure(
                Exception(e.message ?: resourceProvider.getString(R.string.error_update_password))
            )
        }
    }
}