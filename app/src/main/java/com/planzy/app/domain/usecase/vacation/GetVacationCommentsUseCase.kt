package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.VacationsRepository

class GetVacationCommentsUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String): Result<List<VacationComment>> {
        return repository.getVacationComments(vacationId)
    }
}