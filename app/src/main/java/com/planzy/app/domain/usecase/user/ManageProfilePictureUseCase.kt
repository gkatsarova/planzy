package com.planzy.app.domain.usecase.user

import com.planzy.app.domain.repository.UserRepository
import java.io.File

class ManageProfilePictureUseCase(
    private val userRepository: UserRepository
) {
    suspend fun uploadPicture(imageFile: File): Result<String> {
        return userRepository.uploadProfilePicture(imageFile).fold(
            onSuccess = { url ->
                userRepository.updateProfilePictureUrl(url).fold(
                    onSuccess = { Result.success(url) },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun deletePicture(pictureUrl: String): Result<Unit> {
        return userRepository.deleteProfilePicture(pictureUrl)
    }
}
