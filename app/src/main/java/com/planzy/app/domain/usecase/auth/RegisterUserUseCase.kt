package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.user.UserInfo

class RegisterUserUseCase(
    private val authRepository: AuthRepository
) {
    private val TAG = RegisterUserUseCase::class.java.simpleName

    suspend operator fun invoke(
        email: String,
        password: String,
        username: String): Result<UserInfo> {
        return try {
            Log.d(TAG, "Registration started")

            val authResult = authRepository.signUp(email, password, username)
            if (authResult.isFailure) {
                val exception = authResult.exceptionOrNull()
                    ?: AppException(AppError.ERROR_REGISTRATION_FAILED)
                return Result.failure(exception)
            }

            val authUser = authResult.getOrNull()
            if (authUser == null) {
                Log.e(TAG, "Auth user is null after successful signup")
                return Result.failure(AppException(AppError.ERROR_REGISTRATION_FAILED))
            }

            Log.i(TAG, "Auth user created with ID: ${authUser.id}")
            Log.i(TAG, "Verification email sent to: $email")
            Result.success(authUser)
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            val finalException = e as? AppException ?: AppException(AppError.ERROR_REGISTRATION_FAILED)
            Result.failure(finalException)
        }
    }
}