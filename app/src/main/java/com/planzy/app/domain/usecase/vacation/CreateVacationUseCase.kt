package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationsRepository

class CreateVacationUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(title: String): Result<Vacation> {
        return repository.createVacation(title)
    }
}