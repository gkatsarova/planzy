package com.planzy.app.domain.usecase.auth

import android.util.Log
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.user.UserInfo

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    private val TAG = LoginUseCase::class.java.simpleName

    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<UserInfo> {
        return try {
            Log.d(TAG, "Login started for email: $email")

            val authResult = authRepository.signIn(email, password)

            if (authResult.isFailure) {
                val exception = authResult.exceptionOrNull() ?: AppException(AppError.ERROR_LOGIN_FAILED)
                return Result.failure(exception)
            }

            val authUser = authResult.getOrNull()
                ?: return Result.failure(AppException(AppError.ERROR_LOGIN_FAILED))

            if (authUser.emailConfirmedAt == null) {
                Log.w(TAG, "Email not verified for user: ${authUser.email}")
                return Result.failure(AppException(AppError.ERROR_EMAIL_NOT_VERIFIED))
            }

            Log.i(TAG, "Login successful for user: ${authUser.id}")
            return Result.success(authUser)

        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)

            val finalException = e as? AppException ?: AppException(AppError.UNKNOWN_ERROR)
            Result.failure(finalException)
        }
    }
}