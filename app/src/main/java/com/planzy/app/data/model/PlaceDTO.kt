package com.planzy.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDTO(
    val id: String? = null,
    @SerialName("location_id")
    val locationId: String,
    val name: String,
    val address: String?,
    val latitude: String?,
    val longitude: String?,
    val rating: String?,
    val description: String?,
    @SerialName("photo_url")
    val imageUrl: String?,
    val category: String?
)