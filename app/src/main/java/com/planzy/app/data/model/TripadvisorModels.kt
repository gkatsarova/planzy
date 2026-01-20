package com.planzy.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val data: List<SearchResult>? = null
)

@Serializable
data class SearchResult(
    @SerialName("location_id") val locationId: String,
    val name: String,
    val rating: Double? = null,
    @SerialName("num_reviews") val numReviews: Int? = null
)

@Serializable
data class LocationDetailsResponse(
    @SerialName("location_id") val locationId: String,
    val name: String,
    val description: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val rating: Double? = null,
    @SerialName("num_reviews") val numReviews: Int? = null,
    @SerialName("address_obj") val addressObj: Address? = null,
    val phone: String? = null,
    val website: String? = null,
    val photo: Photo? = null,
    val category: Category? = null
)

@Serializable
data class Address(
    @SerialName("address_string") val addressString: String? = null
)

@Serializable
data class Photo(
    val images: Images? = null
)

@Serializable
data class Images(
    val thumbnail: ImageSize? = null,
    val small: ImageSize? = null,
    val medium: ImageSize? = null,
    val large: ImageSize? = null,
    val original: ImageSize? = null
)

@Serializable
data class ImageSize(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class Category(
    val name: String? = null,
    @SerialName("localized_name") val localizedName: String? = null
)

@Serializable
data class PhotosResponse(
    val data: List<PhotoData>? = null
)

@Serializable
data class PhotoData(
    val id: String? = null,
    val images: Images? = null,
    val caption: String? = null,
    @SerialName("published_date") val publishedDate: String? = null
)

@Serializable
data class ReviewsResponse(
    val data: List<ReviewData>? = null
)

@Serializable
data class ReviewData(
    val id: String,
    val title: String? = null,
    val text: String? = null,
    val rating: Int? = null,
    @SerialName("published_date") val publishedDate: String? = null,
    val user: ReviewUser? = null
)

@Serializable
data class ReviewUser(
    val username: String? = null
)