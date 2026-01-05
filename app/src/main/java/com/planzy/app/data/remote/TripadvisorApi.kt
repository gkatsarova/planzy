package com.planzy.app.data.remote

import android.util.Log
import com.planzy.app.data.model.LocationDetailsResponse
import com.planzy.app.data.model.PhotosResponse
import com.planzy.app.data.model.ReviewsResponse
import com.planzy.app.data.model.SearchResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

class TripadvisorApi {

    private val client = TripadvisorClient.httpClient
    private val apiKey = TripadvisorClient.getApiKey()
    private val baseUrl = TripadvisorClient.getBaseUrl()

    suspend fun searchLocations(
        query: String,
        language: String = "en",
        latLong: String? = null,
        radius: Int? = null,
        radiusUnit: String = "km"
    ): Result<SearchResponse> = runCatching {
        Log.d("TripadvisorApi", "Searching for: $query")
        Log.d("TripadvisorApi", "Location: $latLong")
        Log.d("TripadvisorApi", "Using API key: $apiKey")

        client.get("$baseUrl/location/search") {
            parameter("key", apiKey)
            parameter("searchQuery", query)
            parameter("language", language)

            latLong?.let { parameter("latLong", it) }
            radius?.let { parameter("radius", it) }
            parameter("radiusUnit", radiusUnit)
        }.body<SearchResponse>()
    }

    suspend fun getLocationDetails(
        locationId: String,
        language: String = "en"
    ): Result<LocationDetailsResponse> = runCatching {
        client.get("$baseUrl/location/$locationId/details") {
            parameter("key", apiKey)
            parameter("language", language)
        }.body<LocationDetailsResponse>()
    }

    suspend fun getLocationPhotos(
        locationId: String
    ): Result<PhotosResponse> = runCatching {
        client.get("$baseUrl/location/$locationId/photos") {
            parameter("key", apiKey)
        }.body<PhotosResponse>()
    }

    suspend fun getLocationReviews(
        locationId: String,
        language: String = "en"
    ): Result<ReviewsResponse> = runCatching {
        client.get("$baseUrl/location/$locationId/reviews") {
            parameter("key", apiKey)
            parameter("language", language)
        }.body<ReviewsResponse>()
    }
}