package com.planzy.app.domain.usecase.search

import android.util.Log
import com.planzy.app.data.model.User
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.HttpStatusCodes
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.SearchAllParams
import com.planzy.app.domain.model.SearchAllOutcome
import com.planzy.app.domain.model.SearchAllResult
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.repository.VacationsRepository
import com.google.mlkit.nl.entityextraction.Entity

private const val MIN_WORD_LENGTH = 4
private const val DEFAULT_SEARCH_RADIUS = 25
private const val NETWORK_ERROR_HOST = "Unable to resolve host"

class SearchAllUseCase(
    private val placesRepository: PlacesRepository,
    private val vacationsRepository: VacationsRepository,
    private val userRepository: UserRepository,
    private val entityExtractor: LocationEntityExtractor
) {
    private val TAG = SearchAllUseCase::class.java.simpleName
    private val cache = mutableMapOf<String, SearchAllResult>()

    suspend operator fun invoke(params: SearchAllParams): SearchAllOutcome {
        val query = params.query.trim()

        if (query.isBlank()) {
            return SearchAllOutcome.Empty(AppError.ERROR_NO_RESULTS_FOUND)
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

        var placesError: AppError? = null
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
            placesError = mapExceptionToAppError(exception)
            Log.w(TAG, "Places search failed: ${exception.message}")
        }

        val hasAnyResults = users.isNotEmpty() || vacations.isNotEmpty() || places.isNotEmpty()

        return if (!hasAnyResults) {
            SearchAllOutcome.Empty(AppError.ERROR_NO_RESULTS_FOUND)
        } else {
            val result = SearchAllResult(
                places = places,
                vacations = vacations,
                users = users
            )
            cache[query] = result

            if (placesError != null && places.isEmpty() && (users.isNotEmpty() || vacations.isNotEmpty())) {
                SearchAllOutcome.PlacesError(
                    error = placesError,
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

    private fun mapExceptionToAppError(exception: Throwable): AppError {
        val msg = exception.message ?: ""
        return when {
            msg.contains(HttpStatusCodes.TOO_MANY_REQUESTS.toString()) -> AppError.ERROR_API_LIMIT
            msg.contains(HttpStatusCodes.UNAUTHORIZED.toString()) -> AppError.ERROR_UNAUTHORIZED
            msg.contains(NETWORK_ERROR_HOST) -> AppError.ERROR_NO_INTERNET
            else -> AppError.UNKNOWN_ERROR
        }
    }
}