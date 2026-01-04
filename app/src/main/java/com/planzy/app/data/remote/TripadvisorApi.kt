package com.planzy.app.data.remote

import com.planzy.app.data.model.LocationDetailsResponse
import com.planzy.app.data.model.PhotosResponse
import com.planzy.app.data.model.ReviewsResponse
import com.planzy.app.data.model.SearchResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

class TripadvisorApi {

    private val client = TripadvisorClient.httpClient
    private val apiKey = TripadvisorClient.getApiKey()

    suspend fun searchLocations(
        query: String,
        language: String = "en"
    ): Result<SearchResponse> = runCatching {
        client.get("/location/search") {
            parameter("key", apiKey)
            parameter("searchQuery", query)
            parameter("language", language)
        }.body<SearchResponse>()
    }

    suspend fun getLocationDetails(
        locationId: String,
        language: String = "en"
    ): Result<LocationDetailsResponse> = runCatching {
        client.get("/location/$locationId/details") {
            parameter("key", apiKey)
            parameter("language", language)
        }.body<LocationDetailsResponse>()
    }

    suspend fun getLocationPhotos(
        locationId: String
    ): Result<PhotosResponse> = runCatching {
        client.get("/location/$locationId/photos") {
            parameter("key", apiKey)
        }.body<PhotosResponse>()
    }

    suspend fun getLocationReviews(
        locationId: String,
        language: String = "en"
    ): Result<ReviewsResponse> = runCatching {
        client.get("/location/$locationId/reviews") {
            parameter("key", apiKey)
            parameter("language", language)
        }.body<ReviewsResponse>()
    }
}