package com.planzy.app.domain.usecase.auth

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository

class ResendVerificationEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return try {
            val result = authRepository.resendVerificationEmail(email)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                val exception = result.exceptionOrNull()
                    ?: AppException(AppError.ERROR_VERIFICATION_EMAIL_RESEND)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            val finalException = e as? AppException ?: AppException(AppError.ERROR_VERIFICATION_EMAIL_RESEND)
            Result.failure(finalException)
        }
    }
}