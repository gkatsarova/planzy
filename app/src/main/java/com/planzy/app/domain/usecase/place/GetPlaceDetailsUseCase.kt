package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.repository.PlacesRepository

class GetPlaceDetailsUseCase(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(locationId: String): Result<Place> {
        return placesRepository.getPlaceDetails(locationId)
    }
}