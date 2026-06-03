package com.planzy.app.domain.repository

import com.planzy.app.domain.model.VacationPlannerResult

interface VacationPlannerRepository {
    suspend fun createVacationFromText(userMessage: String): Result<VacationPlannerResult>
}