package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class DeleteVacationCommentUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(commentId: String): Result<Unit> {
        return repository.deleteVacationComment(commentId)
    }
}