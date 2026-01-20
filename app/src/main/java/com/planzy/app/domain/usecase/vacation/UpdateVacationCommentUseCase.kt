package com.planzy.app.domain.usecase.vacation

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.VacationsRepository

class UpdateVacationCommentUseCase(
    private val repository: VacationsRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(commentId: String, text: String): Result<Unit> {
        if (text.isBlank()) {
            return Result.failure(Exception(resourceProvider.getString(R.string.empty_comment_text)))
        }
        return repository.updateVacationComment(commentId, text)
    }
}