package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.VacationsRepository

class AddVacationCommentUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String, text: String): Result<VacationComment> {
        if (text.isBlank()) {
            return Result.failure(AppException(AppError.EMPTY_COMMENT_TEXT))
        }
        return repository.addVacationComment(vacationId, text)
    }
}