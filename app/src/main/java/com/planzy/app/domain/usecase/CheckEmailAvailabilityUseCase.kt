package com.planzy.app.domain.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository

class CheckEmailAvailabilityUseCase(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(email: String): Result<Boolean> {
        return try {
            val result = authRepository.checkEmailExistsInAuth(email)
            if (result.isSuccess) {
                Result.success(!(result.getOrNull() ?: false))
            } else {
                val exception = result.exceptionOrNull()
                    ?: Exception(resourceProvider.getString(R.string.error_email_exists))
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}