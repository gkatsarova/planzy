package com.planzy.app.domain.usecase.vacation

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationsRepository

class GetUserVacationsUseCase(
    private val vacationsRepository: VacationsRepository,
    private val resourceProvider: ResourceProvider
) {

    suspend operator fun invoke(): Result<Pair<List<Vacation>, List<Vacation>>> {
        val userVacationsResult = vacationsRepository.getUserVacations()
        val savedVacationsResult = vacationsRepository.getSavedVacations()

        return if (userVacationsResult.isSuccess && savedVacationsResult.isSuccess) {
            val userVacations = userVacationsResult.getOrNull() ?: emptyList()
            val savedVacations = savedVacationsResult.getOrNull() ?: emptyList()

            Result.success(
                Pair(
                    userVacations,
                    savedVacations
                )
            )
        } else {
            val error = userVacationsResult.exceptionOrNull()
                ?: savedVacationsResult.exceptionOrNull()
                ?: Exception(resourceProvider.getString(R.string.unknown_error))
            Result.failure(error)
        }
    }
}