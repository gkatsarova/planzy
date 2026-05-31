package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class ManageSavedVacationUseCase(
    private val vacationsRepository: VacationsRepository
) {
    suspend fun save(vacationId: String): Result<Unit> {
        return vacationsRepository.saveVacation(vacationId)
    }

    suspend fun unsave(vacationId: String): Result<Unit> {
        return vacationsRepository.unsaveVacation(vacationId)
    }

    suspend fun isSaved(vacationId: String): Result<Boolean> {
        return vacationsRepository.isVacationSaved(vacationId)
    }
}
