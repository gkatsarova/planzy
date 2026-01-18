package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class GetVacationCommentsCountUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String): Result<Int> {
        return repository.getVacationCommentsCount(vacationId)
    }
}