package com.planzy.app.data.remote

import com.planzy.app.data.model.*
import com.planzy.app.data.util.HttpStatusCodes
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TripadvisorApi {
    private val client = TripadvisorClient.httpClient
    private val apiKey = TripadvisorClient.getApiKey()
    private val baseUrl = TripadvisorClient.getBaseUrl()

    suspend fun searchLocations(
        query: String,
        latLong: String? = null,
        radius: Int? = null
    ): Result<SearchResponse> = runCatching {
        val response = client.get("$baseUrl/location/search") {
            parameter("key", apiKey)
            parameter("searchQuery", query)
            latLong?.let { parameter("latLong", it) }
            radius?.let { parameter("radius", it) }
        }

        if (response.status.value != HttpStatusCodes.OK) {
            throw Exception("API_ERROR_${response.status.value}")
        }
        response.body<SearchResponse>()
    }

    suspend fun searchNearbyForPlanner(
        latLong: String,
        category: String,
        subCategory: String? = null,
        limit: Int = 10
    ): Result<SearchResponse> = runCatching {
        val response = client.get("$baseUrl/location/nearby_search") {
            parameter("key", apiKey)
            parameter("latLong", latLong)
            parameter("category", category)
            subCategory?.let { parameter("searchQuery", it) }
            parameter("radius", "15")
            parameter("radiusUnit", "km")
            parameter("limit", limit.toString())
        }

        if (!response.status.isSuccess()) {
            throw Exception("TRIPADVISOR_API_ERROR: ${response.status.value}")
        }
        response.body<SearchResponse>()
    }

    suspend fun getLocationDetails(
        locationId: String
    ): Result<LocationDetailsResponse> = runCatching {
        val response = client.get("$baseUrl/location/$locationId/details") {
            parameter("key", apiKey)
        }
        if (!response.status.isSuccess()) throw Exception("API_ERROR_${response.status.value}")
        response.body<LocationDetailsResponse>()
    }

    suspend fun getLocationPhotos(
        locationId: String
    ): Result<PhotosResponse> = runCatching {
        val response = client.get("$baseUrl/location/$locationId/photos") {
            parameter("key", apiKey)
        }
        if (!response.status.isSuccess()) throw Exception("API_ERROR_${response.status.value}")
        response.body<PhotosResponse>()
    }

    suspend fun getLocationReviews(
        locationId: String,
        limit: Int = 5
    ): Result<ReviewsResponse> = runCatching {
        val response = client.get("$baseUrl/location/$locationId/reviews") {
            parameter("key", apiKey)
            parameter("limit", limit)
        }
        if (!response.status.isSuccess()) throw Exception("API_ERROR_${response.status.value}")
        response.body<ReviewsResponse>()
    }
}