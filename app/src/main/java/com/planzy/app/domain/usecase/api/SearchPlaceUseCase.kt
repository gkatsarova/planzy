package com.planzy.app.domain.usecase.api

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.repository.PlacesRepository

class SearchPlacesUseCase(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(
        destination: String,
        minRating: Double = 3.0,
        maxResults: Int = 10,
        latLong: String? = null,
        radius: Int? = null
    ): Result<List<Place>> = placesRepository.searchPlaces(
        query = destination,
        minRating = minRating,
        latLong = latLong,
        radius = radius
    ).mapCatching { places ->
        places.sortedByDescending { it.rating }.take(maxResults)
    }
}