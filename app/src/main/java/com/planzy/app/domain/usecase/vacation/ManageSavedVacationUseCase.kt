package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class ManageSavedVacationUseCase(
    private val vacationsRepository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String): Result<Boolean> {
        val isSaved = vacationsRepository.isVacationSaved(vacationId).getOrElse { return Result.failure(it) }
        return if (isSaved) {
            vacationsRepository.unsaveVacation(vacationId).map { false }
        } else {
            vacationsRepository.saveVacation(vacationId).map { true }
        }
    }
}
