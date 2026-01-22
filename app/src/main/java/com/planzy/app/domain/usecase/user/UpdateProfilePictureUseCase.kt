package com.planzy.app.domain.usecase.user

import com.planzy.app.domain.repository.UserRepository

class UpdateProfilePictureUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(url: String): Result<Unit> {
        return userRepository.updateProfilePictureUrl(url)
    }
}