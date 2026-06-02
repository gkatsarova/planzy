package com.planzy.app.domain.usecase.search

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.data.util.HttpStatusCodes
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.repository.VacationsRepository
import com.google.mlkit.nl.entityextraction.Entity

private const val MIN_WORD_LENGTH = 4
private const val DEFAULT_SEARCH_RADIUS = 25
private const val NETWORK_ERROR_HOST = "Unable to resolve host"

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

class SearchAllUseCase(
    private val placesRepository: PlacesRepository,
    private val vacationsRepository: VacationsRepository,
    private val userRepository: UserRepository,
    private val entityExtractor: LocationEntityExtractor,
    private val resourceProvider: ResourceProvider
) {
    private val TAG = SearchAllUseCase::class.java.simpleName
    private val cache = mutableMapOf<String, SearchAllResult>()

    suspend operator fun invoke(params: SearchAllParams): SearchAllOutcome {
        val query = params.query.trim()

        if (query.isBlank()) {
            return SearchAllOutcome.Empty(
                resourceProvider.getString(R.string.error_no_results_found)
            )
        }

        cache[query]?.let { cached ->
            Log.d(TAG, "Cache hit for '$query'")
            return SearchAllOutcome.Success(cached)
        }

        val users = searchUsers(query)
        Log.d(TAG, "Found ${users.size} users for '$query'")

        val vacations = searchVacations(query)
        Log.d(TAG, "Found ${vacations.size} vacations for '$query'")

        val foundLocationInText = detectLocationInQuery(query)
        val (latLong, radius) = buildSearchParams(
            foundLocationInText, params.locationPermissionGranted, params.userLocation
        )
        Log.d(TAG, "Location detected=$foundLocationInText, GPS active=${latLong != null}")

        var placesError: String? = null
        val places = mutableListOf<Place>()

        placesRepository.searchPlaces(
            query = query,
            minRating = 3.0,
            latLong = latLong,
            radius = radius
        ).onSuccess { list ->
            places.addAll(list.sortedByDescending { it.rating }.take(10))
            Log.d(TAG, "Found ${places.size} places for '$query'")
        }.onFailure { exception ->
            placesError = mapExceptionToErrorMessage(exception)
            Log.w(TAG, "Places search failed: ${exception.message}")
        }

        val hasAnyResults = users.isNotEmpty() || vacations.isNotEmpty() || places.isNotEmpty()

        return if (!hasAnyResults) {
            SearchAllOutcome.Empty(
                resourceProvider.getString(R.string.error_no_results_found)
            )
        } else {
            val result = SearchAllResult(
                places = places,
                vacations = vacations,
                users = users
            )
            cache[query] = result

            if (placesError != null && places.isEmpty() && (users.isNotEmpty() || vacations.isNotEmpty())) {
                SearchAllOutcome.PlacesError(
                    message = placesError,
                    partialResult = result
                )
            } else {
                SearchAllOutcome.Success(result)
            }
        }
    }

    fun clearCache() = cache.clear()
    private suspend fun searchUsers(query: String): List<User> =
        userRepository.searchUsers(query).getOrElse {
            Log.e(TAG, "User search error: ${it.message}")
            emptyList()
        }

    private suspend fun searchVacations(query: String): List<Vacation> =
        vacationsRepository.searchVacations(query).getOrElse {
            Log.e(TAG, "Vacation search error: ${it.message}")
            emptyList()
        }

    private suspend fun detectLocationInQuery(query: String): Boolean {
        val words = query.split(" ").filter { it.isNotBlank() }
        for (word in words) {
            val testWord = word.lowercase().replaceFirstChar { it.uppercase() }
            val annotation = entityExtractor.extractLocation(testWord)
            val isMlAddress =
                annotation?.entities?.any { it.type == Entity.TYPE_ADDRESS } ?: false
            if (isMlAddress || (word.length >= MIN_WORD_LENGTH && words.size > 1)) {
                return true
            }
        }
        return false
    }

    private fun buildSearchParams(
        foundLocationInText: Boolean,
        permissionGranted: Boolean,
        userLocation: Pair<Double, Double>?
    ): Pair<String?, Int?> {
        val shouldUseGps = permissionGranted && userLocation != null && !foundLocationInText
        return if (shouldUseGps) {
            "${userLocation.first},${userLocation.second}" to DEFAULT_SEARCH_RADIUS
        } else {
            null to null
        }
    }

    private fun mapExceptionToErrorMessage(exception: Throwable): String {
        val msg = exception.message ?: ""
        val resId = when {
            msg.contains(HttpStatusCodes.TOO_MANY_REQUESTS.toString()) -> R.string.error_api_limit
            msg.contains(HttpStatusCodes.UNAUTHORIZED.toString()) -> R.string.error_unauthorized
            msg.contains(NETWORK_ERROR_HOST) -> R.string.error_no_internet
            else -> R.string.error_unknown
        }
        return resourceProvider.getString(resId)
    }
}