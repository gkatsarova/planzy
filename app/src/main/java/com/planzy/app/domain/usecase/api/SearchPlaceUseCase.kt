package com.planzy.app.domain.usecase.api

import android.util.Log
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.repository.PlacesRepository

class SearchPlacesUseCase(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(
        destination: String,
        minRating: Double = 3.0,
        maxResults: Int = 20,
        latLong: String? = null,
        radius: Int? = null
    ): Result<List<Place>> {
        Log.d("SearchPlacesUseCase", "Searching: destination=$destination, minRating=$minRating, maxResults=$maxResults")
        Log.d("SearchPlacesUseCase", "Location: $latLong, radius: $radius km")

        return placesRepository.searchPlaces(
            query = destination,
            minRating = minRating,
            latLong = latLong,
            radius = radius
        ).mapCatching { places ->
            Log.d("SearchPlacesUseCase", "Repository returned ${places.size} places")

            val filtered = places
                .filter { place ->
                    val hasEnoughReviews = place.reviewsCount > 5
                    Log.d("SearchPlacesUseCase", "${place.name}: reviews=${place.reviewsCount} -> include=$hasEnoughReviews")
                    hasEnoughReviews
                }
                .sortedByDescending { it.rating }
                .take(maxResults)

            Log.d("SearchPlacesUseCase", "Final result: ${filtered.size} places")
            filtered
        }
    }
}