package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.VacationPlace
import com.planzy.app.domain.repository.VacationsRepository

class AddPlaceToVacationUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(vacationId: String, placeId: String): Result<VacationPlace> {
        return repository.addPlaceToVacation(vacationId, placeId)
    }
}