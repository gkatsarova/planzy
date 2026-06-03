package com.planzy.app.domain.usecase.auth

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository

class CheckEmailAvailabilityUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Boolean> {
        return try {
            val result = authRepository.checkEmailExistsInAuth(email)
            if (result.isSuccess) {
                Result.success(!(result.getOrNull() ?: false))
            } else {
                val exception = result.exceptionOrNull()
                    ?: AppException(AppError.ERROR_EMAIL_EXISTS)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}