package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationsRepository

class GetUserVacationsUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(): Result<List<Vacation>> {
        return repository.getUserVacations()
    }
}