package com.planzy.app.domain.usecase.api

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.repository.PlacesRepository

class SearchPlacesUseCase(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(
        destination: String,
        minRating: Double = 4.0,
        maxResults: Int = 20
    ): Result<List<Place>> {
        return placesRepository.searchPlaces(
            query = destination,
            minRating = minRating
        ).mapCatching { places ->
            places
                .filter { it.reviewsCount > 5 }
                .sortedByDescending { it.rating }
                .take(maxResults)
        }
    }
}