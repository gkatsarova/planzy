package com.planzy.app.domain.repository

import com.planzy.app.data.model.VacationPlannerResponse

interface VacationPlannerRepository {
    suspend fun createVacationFromText(userMessage: String): Result<VacationPlannerResponse>
}