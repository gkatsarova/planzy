package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationsRepository

class GetFollowedUsersVacationsUseCase(
    private val vacationsRepository: VacationsRepository
) {
    suspend operator fun invoke(): Result<List<Vacation>> {
        return vacationsRepository.getFollowedUsersVacations()
    }
}