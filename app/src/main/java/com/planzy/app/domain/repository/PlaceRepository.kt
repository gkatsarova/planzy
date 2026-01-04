package com.planzy.app.domain.repository

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview

interface PlacesRepository {

    suspend fun searchPlaces(
        query: String,
        minRating: Double = 4.0
    ): Result<List<Place>>

    suspend fun getPlaceDetails(
        locationId: String
    ): Result<Place>

    suspend fun getPlacePhotos(
        locationId: String
    ): Result<List<String>>

    suspend fun getPlaceReviews(
        locationId: String,
        limit: Int = 5
    ): Result<List<PlaceReview>>
}