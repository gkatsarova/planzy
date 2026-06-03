package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.model.VacationDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetVacationDataUseCase(
    private val vacationsRepository: VacationsRepository,
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(vacationId: String): Result<VacationDetails> = coroutineScope {
        val vacationWithUserDeferred = async { vacationsRepository.getVacationWithUser(vacationId) }
        val placeIdsDeferred = async { vacationsRepository.getVacationPlaceIds(vacationId) }
        val commentsDeferred = async { vacationsRepository.getVacationComments(vacationId) }

        try {
            val (vacation, username) = vacationWithUserDeferred.await().getOrThrow()
            val comments = commentsDeferred.await().getOrThrow()
            val placeIds = placeIdsDeferred.await().getOrThrow()

            val places = placeIds.map { placeId ->
                async {
                    placesRepository.getPlaceDetails(placeId).getOrNull()
                }
            }.awaitAll().filterNotNull()

            Result.success(
                VacationDetails(
                    vacation = vacation,
                    creatorUsername = username,
                    places = places,
                    vacationComments = comments
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}