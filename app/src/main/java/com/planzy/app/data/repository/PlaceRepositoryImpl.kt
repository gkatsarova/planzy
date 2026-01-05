package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.data.model.toDomainModel
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.repository.PlacesRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class PlacesRepositoryImpl(
    private val tripadvisorApi: TripadvisorApi
) : PlacesRepository {

    private val semaphore = Semaphore(1)

    private suspend fun <T> retryOnRateLimit(
        maxRetries: Int = 3,
        initialDelay: Long = 5000,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            val result = block()

            if (result.isSuccess) {
                return result
            }

            val exception = result.exceptionOrNull()
            if (exception?.message?.contains("429") == true ||
                exception?.message?.contains("Limit Exceeded") == true) {

                if (attempt < maxRetries - 1) {
                    Log.w("PlacesRepository", "Rate limited, waiting ${currentDelay}ms before retry ${attempt + 1}")
                    delay(currentDelay)
                    currentDelay *= 2
                } else {
                    Log.e("PlacesRepository", "Max retries reached, giving up")
                }
            } else {
                return result
            }
        }

        return block()
    }

    override suspend fun searchPlaces(
        query: String,
        minRating: Double,
        latLong: String?,
        radius: Int?
    ): Result<List<Place>> = coroutineScope {
        tripadvisorApi.searchLocations(
            query = query,
            latLong = latLong,
            radius = radius
        )
            .mapCatching { response ->
                Log.d("PlacesRepository", "Search response data size: ${response.data?.size}")

                val places = response.data
                    ?.take(3)
                    ?.mapNotNull { searchResult ->
                        semaphore.withPermit {
                            Log.d("PlacesRepository", "Fetching details for: ${searchResult.name} (ID: ${searchResult.locationId})")

                            try {
                                delay(2000)

                                val detailsResult = retryOnRateLimit {
                                    tripadvisorApi.getLocationDetails(searchResult.locationId)
                                }

                                val details = detailsResult.getOrNull()

                                if (details != null) {
                                    Log.d("PlacesRepository", "Got details for: ${details.name} - Rating: ${details.rating}, Reviews: ${details.numReviews}")
                                    details.toDomainModel()
                                } else {
                                    Log.e("PlacesRepository", "Failed to get details for ${searchResult.name}")
                                    null
                                }
                            } catch (e: Exception) {
                                Log.e("PlacesRepository", "Exception for ${searchResult.name}: ${e.message}")
                                null
                            }
                        }
                    }
                    ?.filter { place ->
                        val meetsRating = place.rating >= minRating
                        val meetsReviews = place.reviewsCount > 5

                        Log.d("PlacesRepository", "${place.name}: rating=${place.rating} (min=$minRating), reviews=${place.reviewsCount} -> include=${meetsRating && meetsReviews}")

                        meetsRating && meetsReviews
                    }
                    ?.sortedByDescending { it.reviewsCount }
                    ?: emptyList()

                Log.d("PlacesRepository", "Final filtered places: ${places.size}")
                places
            }
    }

    override suspend fun getPlaceDetails(
        locationId: String
    ): Result<Place> = coroutineScope {
        semaphore.withPermit {
            try {
                delay(500)

                val detailsResult = retryOnRateLimit {
                    tripadvisorApi.getLocationDetails(locationId)
                }

                val details = detailsResult.getOrThrow()
                val place = details.toDomainModel()

                Result.success(place)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getPlacePhotos(
        locationId: String
    ): Result<List<String>> {
        return semaphore.withPermit {
            delay(500)

            retryOnRateLimit {
                tripadvisorApi.getLocationPhotos(locationId)
            }.mapCatching { response ->
                response.data
                    ?.mapNotNull { it.images?.large?.url }
                    ?: emptyList()
            }
        }
    }

    override suspend fun getPlaceReviews(
        locationId: String,
        limit: Int
    ): Result<List<PlaceReview>> {
        return semaphore.withPermit {
            delay(500)

            retryOnRateLimit {
                tripadvisorApi.getLocationReviews(locationId)
            }.mapCatching { response ->
                response.data
                    ?.take(limit)
                    ?.map { it.toDomainModel() }
                    ?: emptyList()
            }
        }
    }
}