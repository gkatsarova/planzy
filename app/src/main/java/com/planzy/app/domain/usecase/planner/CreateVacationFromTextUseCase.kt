package com.planzy.app.domain.usecase.planner

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.VacationPlannerResult
import com.planzy.app.domain.repository.VacationPlannerRepository

class CreateVacationFromTextUseCase(
    private val repository: VacationPlannerRepository
) {
    suspend operator fun invoke(userMessage: String): Result<VacationPlannerResult> {
        if (userMessage.isBlank()) {
            return Result.failure(AppException(AppError.DESCRIBE_YOUR_DREAM_VACATION))
        }

        return repository.createVacationFromText(userMessage.trim())
    }
}