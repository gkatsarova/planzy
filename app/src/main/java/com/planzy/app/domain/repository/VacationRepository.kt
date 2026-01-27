package com.planzy.app.domain.repository

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationComment
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
    suspend fun getVacationComments(
        vacationId: String
    ): Result<List<VacationComment>>
    suspend fun addVacationComment(
        vacationId: String,
        text: String
    ): Result<VacationComment>
    suspend fun updateVacationComment(
        commentId: String,
        text: String
    ): Result<Unit>
    suspend fun deleteVacationComment(
        commentId: String
    ): Result<Unit>
    suspend fun getVacationCommentsCount(
        vacationId: String
    ): Result<Int>

    suspend fun saveVacation(
        vacationId: String
    ): Result<Unit>

    suspend fun unsaveVacation(
        vacationId: String
    ): Result<Unit>

    suspend fun getSavedVacations(): Result<List<Vacation>>

    suspend fun isVacationSaved(
        vacationId: String
    ): Result<Boolean>

    suspend fun deleteVacation(
        vacationId: String
    ): Result<Unit>

    suspend fun getUserVacationsById(
        userId: String
    ): Result<List<Vacation>>

    suspend fun getFollowedUsersVacations(): Result<List<Vacation>>
}