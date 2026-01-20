package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationsRepository

class SearchVacationsUseCase(
    private val repository: VacationsRepository
) {
    suspend operator fun invoke(query: String): Result<List<Vacation>> {
        return repository.searchVacations(query)
    }
}