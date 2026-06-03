package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.VacationsRepository

class UpdateVacationCommentUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(commentId: String, text: String): Result<Unit> {
        if (text.isBlank()) {
            return Result.failure(AppException(AppError.EMPTY_COMMENT_TEXT))
        }
        return repository.updateVacationComment(commentId, text)
    }
}