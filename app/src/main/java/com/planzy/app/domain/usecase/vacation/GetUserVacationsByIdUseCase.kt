package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationsRepository

class GetUserVacationsByIdUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Vacation>> {
        return repository.getUserVacationsById(userId)
    }
}