package com.planzy.app.domain.usecase.user

import com.planzy.app.domain.repository.UserRepository

class DeleteProfilePictureUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(pictureUrl: String): Result<Unit> {
        return userRepository.deleteProfilePicture(pictureUrl)
    }
}