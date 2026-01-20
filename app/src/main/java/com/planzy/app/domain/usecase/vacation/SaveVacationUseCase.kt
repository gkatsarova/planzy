package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class SaveVacationUseCase(
    private val vacationsRepository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String): Result<Unit> {
        return vacationsRepository.saveVacation(vacationId)
    }
}