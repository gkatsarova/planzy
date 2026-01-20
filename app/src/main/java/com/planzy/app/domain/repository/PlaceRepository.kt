package com.planzy.app.domain.repository

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.model.UserComment

interface PlacesRepository {

    suspend fun searchPlaces(
        query: String,
        minRating: Double = 4.0,
        latLong: String? = null,
        radius: Int? = null
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

    suspend fun getUserComments(
        placeId: String
    ): Result<List<UserComment>>

    suspend fun addUserComment(
        placeId: String,
        text: String,
        rating: Int
    ): Result<UserComment>

    suspend fun updateUserComment(
        commentId: String,
        text: String,
        rating: Int
    ): Result<Unit>

    suspend fun deleteUserComment(
        commentId: String
    ): Result<Unit>

    suspend fun getUserCommentsStats(
        placeId: String
    ): Result<Pair<Double?, Int>>

    suspend fun savePlace(
        place: Place
    ): Result<Unit>
}