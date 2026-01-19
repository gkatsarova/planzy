package com.planzy.app.domain.usecase.planner

import com.planzy.app.R
import com.planzy.app.data.model.VacationPlannerResponse
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.VacationPlannerRepository

class CreateVacationFromTextUseCase(
    private val repository: VacationPlannerRepository,
    private val recourseProvider: ResourceProvider
) {
    suspend operator fun invoke(userMessage: String): Result<VacationPlannerResponse> {
        if (userMessage.isBlank()) {
            return Result.success(
                VacationPlannerResponse.Error(recourseProvider.getString(R.string.describe_your_dream_vacation))
            )
        }

        return repository.createVacationFromText(userMessage.trim())
    }
}