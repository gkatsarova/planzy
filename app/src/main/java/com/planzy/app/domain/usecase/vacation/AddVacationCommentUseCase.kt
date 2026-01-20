package com.planzy.app.domain.usecase.vacation

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.VacationsRepository

class AddVacationCommentUseCase(
    private val repository: VacationsRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(vacationId: String, text: String): Result<VacationComment> {
        if (text.isBlank()) {
            return Result.failure(Exception(resourceProvider.getString(R.string.empty_comment_text)))
        }
        return repository.addVacationComment(vacationId, text)
    }
}