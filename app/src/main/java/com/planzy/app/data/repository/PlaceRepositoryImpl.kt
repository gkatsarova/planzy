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
        minRating: Double,
        latLong: String?,
        radius: Int?
    ): Result<List<Place>> {
        return try {
            val response = tripadvisorApi.searchLocations(query, latLong = latLong, radius = radius).getOrThrow()
            val results = response.data ?: emptyList()

            val places = results.mapNotNull { item ->
                val details = tripadvisorApi.getLocationDetails(item.locationId).getOrNull()
                if (details != null) {
                    var domainPlace = details.toDomainModel()

                    if (domainPlace.photoUrl == null) {
                        val photos = tripadvisorApi.getLocationPhotos(item.locationId).getOrNull()
                        domainPlace = domainPlace.copy(photoUrl = photos?.data?.firstOrNull()?.images?.large?.url)
                    }
                    domainPlace
                } else null
            }
            Result.success(places)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaceDetails(locationId: String) =
        tripadvisorApi.getLocationDetails(locationId).map { it.toDomainModel() }

    override suspend fun getPlacePhotos(locationId: String) = Result.success(emptyList<String>())
    override suspend fun getPlaceReviews(locationId: String, limit: Int) = Result.success(emptyList<PlaceReview>())
}