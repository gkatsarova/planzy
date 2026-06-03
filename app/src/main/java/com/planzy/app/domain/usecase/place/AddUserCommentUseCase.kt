package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository

class AddUserCommentUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(placeId: String, text: String, rating: Int): Result<UserComment> {
        if (text.isBlank()) {
            return Result.failure(AppException(AppError.EMPTY_COMMENT_TEXT))
        }
        if (rating !in 1..5) {
            return Result.failure(AppException(AppError.RATING_ERROR))
        }
        return repository.addUserComment(placeId, text, rating)
    }
}