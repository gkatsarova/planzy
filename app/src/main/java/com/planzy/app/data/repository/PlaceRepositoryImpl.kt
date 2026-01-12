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

            val results = response.data ?: emptyList()

            val places = results.mapNotNull { item ->
                val locationId = item.locationId
                if (locationId.isBlank()) return@mapNotNull null

                val detailsResult = tripadvisorApi.getLocationDetails(locationId).getOrThrow()
                var domainPlace = detailsResult.toDomainModel()

                if (domainPlace.photoUrl.isNullOrEmpty()) {
                    val photosResult = tripadvisorApi.getLocationPhotos(locationId).getOrNull()
                    val photoUrl = photosResult?.data?.firstOrNull()?.images?.large?.url
                    if (photoUrl != null) {
                        domainPlace = domainPlace.copy(photoUrl = photoUrl)
                    }
                }
                domainPlace
            }

            Result.success(places)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaceDetails(locationId: String): Result<Place> {
        return try {
            val detailsResult = tripadvisorApi.getLocationDetails(locationId).getOrThrow()
            var domainPlace = detailsResult.toDomainModel()

            if (domainPlace.photoUrl.isNullOrEmpty()) {
                val photosResult = tripadvisorApi.getLocationPhotos(locationId).getOrNull()
                val photoUrl = photosResult?.data?.firstOrNull()?.images?.large?.url
                    ?: photosResult?.data?.firstOrNull()?.images?.medium?.url

                if (photoUrl != null) {
                    domainPlace = domainPlace.copy(photoUrl = photoUrl)
                }
            }

            Result.success(domainPlace)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlacePhotos(locationId: String): Result<List<String>> =
        tripadvisorApi.getLocationPhotos(locationId).map { response ->
            response.data?.mapNotNull { it.images?.large?.url } ?: emptyList()
        }

    override suspend fun getPlaceReviews(locationId: String, limit: Int): Result<List<PlaceReview>> {
        return try {
            val reviewsResponse = tripadvisorApi.getLocationReviews(locationId, limit).getOrThrow()
            val reviews = reviewsResponse.data?.map { it.toDomainModel() } ?: emptyList()
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}