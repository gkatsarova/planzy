package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository

class GetPlaceDataUseCase(
    private val repository: PlacesRepository
) {
    suspend fun getPlaceDetails(locationId: String): Result<Place> {
        return repository.getPlaceDetails(locationId)
    }

    suspend fun getPlaceReviews(locationId: String, limit: Int): Result<List<PlaceReview>> {
        return repository.getPlaceReviews(locationId, limit)
    }

    suspend fun getUserComments(locationId: String): Result<List<UserComment>> {
        return repository.getUserComments(locationId)
    }
}
