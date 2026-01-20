package com.planzy.app.domain.model

data class Place(
    val id: String,
    val name: String,
    val location: Location,
    val rating: Double,
    val reviewsCount: Int,
    val description: String?,
    val photoUrl: String?,
    val category: String?,
    val contact: ContactInfo?
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class ContactInfo(
    val phone: String?,
    val website: String?
)

data class PlaceReview(
    val id: String,
    val author: String,
    val rating: Int,
    val text: String?,
    val date: String
)