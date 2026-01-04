package com.planzy.app.data.repository

import com.planzy.app.data.model.toDomainModel
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.repository.PlacesRepository

class PlacesRepositoryImpl(
    private val tripadvisorApi: TripadvisorApi
) : PlacesRepository {

    override suspend fun searchPlaces(
        query: String,
        minRating: Double
    ): Result<List<Place>> {
        return tripadvisorApi.searchLocations(query)
            .mapCatching { response ->
                response.data
                    ?.filter { it.rating != null && it.rating >= minRating }
                    ?.sortedByDescending { it.numReviews }
                    ?.mapNotNull { searchResult ->
                        tripadvisorApi.getLocationDetails(searchResult.locationId)
                            .getOrNull()
                            ?.toDomainModel()
                    }
                    ?: emptyList()
            }
    }

    override suspend fun getPlaceDetails(
        locationId: String
    ): Result<Place> {
        return tripadvisorApi.getLocationDetails(locationId)
            .mapCatching { it.toDomainModel() }
    }

    override suspend fun getPlacePhotos(
        locationId: String
    ): Result<List<String>> {
        return tripadvisorApi.getLocationPhotos(locationId)
            .mapCatching { response ->
                response.data
                    ?.mapNotNull { it.images?.large?.url }
                    ?: emptyList()
            }
    }

    override suspend fun getPlaceReviews(
        locationId: String,
        limit: Int
    ): Result<List<PlaceReview>> {
        return tripadvisorApi.getLocationReviews(locationId)
            .mapCatching { response ->
                response.data
                    ?.take(limit)
                    ?.map { it.toDomainModel() }
                    ?: emptyList()
            }
    }
}