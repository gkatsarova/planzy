package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.VacationsRepository

class RemovePlaceFromVacationUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String, placeId: String): Result<Unit> {
        return repository.removePlaceFromVacation(vacationId, placeId)
    }
}