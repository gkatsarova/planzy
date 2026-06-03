package com.planzy.app.domain.usecase.auth

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.UserRepository

class CheckUsernameAvailabilityUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Result<Boolean> {
        return try {
            val result = userRepository.getUserByUsername(username)
            if (result.isSuccess) {
                Result.success(result.getOrNull() == null)
            } else {
                val exception = result.exceptionOrNull()
                    ?: AppException(AppError.ERROR_USERNAME_EXISTS)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}