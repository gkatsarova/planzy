package com.planzy.app.domain.usecase.api

import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.repository.PlacesRepository

class GetPlaceReviewsUseCase(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(locationId: String, limit: Int = 5): Result<List<PlaceReview>> {
        return placesRepository.getPlaceReviews(locationId, limit)
    }
}