package com.planzy.app.data.model

import com.planzy.app.domain.model.Vacation
import kotlinx.serialization.Serializable

@Serializable
data class VacationPreferences(
    val hotelCount: Int = 1,
    val restaurantCount: Int = 6,
    val attractionCount: Int = 3,
    val nightlifeCount: Int = 0,
    val categoryFilter: String? = null
)

data class VacationIntent(
    val destination: String,
    val durationDays: Int,
    val theme: String? = null,
    val preferences: VacationPreferences = VacationPreferences()
)

sealed class VacationPlannerResponse {
    data class Success(
        val vacation: Vacation,
        val placesAdded: Int,
        val message: String
    ) : VacationPlannerResponse()
    data class Error(val message: String) : VacationPlannerResponse()
}

enum class PlaceCategory(val apiValue: String) {
    HOTEL("hotels"),
    RESTAURANT("restaurants"),
    ATTRACTION("attractions")
}