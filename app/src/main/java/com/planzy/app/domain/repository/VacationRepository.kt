package com.planzy.app.domain.repository

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationPlace

interface VacationsRepository {
    suspend fun getUserVacations(): Result<List<Vacation>>

    suspend fun createVacation(
        title: String
    ): Result<Vacation>

    suspend fun addPlaceToVacation(
        vacationId: String,
        placeId: String
    ): Result<VacationPlace>

    suspend fun searchVacations(
        query: String
    ): Result<List<Vacation>>

    suspend fun getVacationWithUser(
        vacationId: String
    ): Result<Pair<Vacation, String>>
    suspend fun getVacationPlaceIds(
        vacationId: String
    ): Result<List<String>>
    suspend fun removePlaceFromVacation(
        vacationId: String,
        placeId: String
    ): Result<Unit>
}