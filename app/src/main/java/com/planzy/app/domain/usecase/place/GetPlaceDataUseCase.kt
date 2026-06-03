package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.model.PlaceDetailsData
import com.planzy.app.domain.repository.PlacesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetPlaceDataUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(locationId: String, reviewLimit: Int = 5): Result<PlaceDetailsData> = coroutineScope {
        val detailsDeferred = async { repository.getPlaceDetails(locationId) }
        val reviewsDeferred = async { repository.getPlaceReviews(locationId, reviewLimit) }
        val commentsDeferred = async { repository.getUserComments(locationId) }

        try {
            Result.success(
                PlaceDetailsData(
                    place = detailsDeferred.await().getOrThrow(),
                    reviews = reviewsDeferred.await().getOrThrow(),
                    userComments = commentsDeferred.await().getOrThrow()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}