package com.planzy.app.domain.usecase.auth

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.UserRepository

class CheckUsernameAvailabilityUseCase(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(username: String): Result<Boolean> {
        return try {
            val result = userRepository.getUserByUsername(username)
            if (result.isSuccess) {
                Result.success(result.getOrNull() == null)
            } else {
                val exception = result.exceptionOrNull()
                    ?: Exception(resourceProvider.getString(R.string.error_username_exists))
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}