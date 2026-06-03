package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.PlacesRepository

class UpdateUserCommentUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(
        commentId: String,
        text: String,
        rating: Int
    ): Result<Unit> {
        if (text.isBlank()) {
            return Result.failure(AppException(AppError.EMPTY_COMMENT_TEXT))
        }
        if (rating !in 1..5) {
            return Result.failure(AppException(AppError.RATING_ERROR))
        }
        return repository.updateUserComment(commentId, text, rating)
    }
}