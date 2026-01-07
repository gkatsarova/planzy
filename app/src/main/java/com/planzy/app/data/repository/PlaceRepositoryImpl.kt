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
            val response = tripadvisorApi.searchLocations(
                query = query,
                latLong = latLong,
                radius = radius
            ).getOrThrow()

            val results = response.data ?: listOf()

            val places = results.mapNotNull { item ->
                val locationId = item.locationId
                if (locationId.isBlank()) return@mapNotNull null

                val detailsResult = tripadvisorApi.getLocationDetails(locationId).getOrNull()
                if (detailsResult == null) return@mapNotNull null

                var domainPlace = detailsResult.toDomainModel()

                if (domainPlace.photoUrl == null || domainPlace.photoUrl.isEmpty()) {
                    val photosResult = tripadvisorApi.getLocationPhotos(locationId).getOrNull()
                    val photoUrl = photosResult?.data?.firstOrNull()?.images?.large?.url

                    domainPlace = Place(
                        id = domainPlace.id,
                        name = domainPlace.name,
                        location = domainPlace.location,
                        rating = domainPlace.rating,
                        reviewsCount = domainPlace.reviewsCount,
                        description = domainPlace.description,
                        photoUrl = photoUrl ?: domainPlace.photoUrl,
                        category = domainPlace.category,
                        contact = domainPlace.contact
                    )
                }

                domainPlace
            }
            Result.success(places)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaceDetails(locationId: String): Result<Place> =
        tripadvisorApi.getLocationDetails(locationId).map { it.toDomainModel() }

    override suspend fun getPlacePhotos(locationId: String): Result<List<String>> =
        Result.success(listOf())

    override suspend fun getPlaceReviews(locationId: String, limit: Int): Result<List<PlaceReview>> =
        Result.success(listOf())
}