package com.planzy.app.domain.usecase.user

import com.planzy.app.domain.repository.UserRepository
import java.io.File

class UploadProfilePictureUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imageFile: File): Result<String> {
        val url = userRepository.uploadProfilePicture(imageFile)
            .getOrElse { return Result.failure(it) }

        return userRepository.updateProfilePictureUrl(url)
            .map { url }
    }
}