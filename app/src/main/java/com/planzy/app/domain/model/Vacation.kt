package com.planzy.app.domain.model

data class Vacation(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String,
    val placesCount: Int = 0
)

data class VacationPlace(
    val id: String,
    val vacationId: String,
    val placeId: String,
    val orderIndex: Int,
    val createdAt: String
)