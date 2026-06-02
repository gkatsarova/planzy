package com.planzy.app.domain.model

import com.planzy.app.data.model.User

data class SearchAllParams(
    val query: String,
    val userLocation: Pair<Double, Double>? = null,
    val locationPermissionGranted: Boolean = false
)

data class SearchAllResult(
    val places: List<Place> = emptyList(),
    val vacations: List<Vacation> = emptyList(),
    val users: List<User> = emptyList()
)

sealed class SearchAllOutcome {
    data class Success(val result: SearchAllResult) : SearchAllOutcome()
    data class Empty(val message: String) : SearchAllOutcome()
    data class PlacesError(
        val message: String,
        val partialResult: SearchAllResult
    ) : SearchAllOutcome()
}