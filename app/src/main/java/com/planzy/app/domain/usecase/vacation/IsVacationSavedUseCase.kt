package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class IsVacationSavedUseCase(
    private val vacationsRepository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String): Result<Boolean> {
        return vacationsRepository.isVacationSaved(vacationId)
    }
}