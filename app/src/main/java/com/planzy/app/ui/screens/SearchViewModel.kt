package com.planzy.app.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.usecase.place.GetUserCommentsStatsUseCase
import com.planzy.app.domain.usecase.place.SearchPlacesUseCase
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.google.mlkit.nl.entityextraction.Entity
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.HttpStatusCodes.TOO_MANY_REQUESTS
import com.planzy.app.data.util.HttpStatusCodes.UNAUTHORIZED
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.SearchVacationsUseCase

data class PlaceWithStats(
    val place: Place,
    val userRating: Double?,
    val userReviewsCount: Int
)

class SearchViewModel(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase,
    private val searchVacationsUseCase: SearchVacationsUseCase,
    private val entityExtractor: LocationEntityExtractor,
    private val resourceProvider: ResourceProvider,
    context: Context
) : ViewModel() {

    companion object {
        private val TAG = SearchViewModel::class.java.simpleName
        private const val MIN_WORD_LENGTH = 4
        private const val DEFAULT_SEARCH_RADIUS = 25
        private const val NETWORK_ERROR_HOST = "Unable to resolve host"
    }

    private val prefs = context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE)
    private val searchCache = mutableMapOf<String, List<PlaceWithStats>>()

    var searchQuery by mutableStateOf("")
        private set

    var places by mutableStateOf<List<Place>>(emptyList())
        private set
    var placesWithStats by mutableStateOf<List<PlaceWithStats>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userLocation by mutableStateOf<Pair<Double, Double>?>(null)
        private set
    var locationPermissionGranted by mutableStateOf(prefs.getBoolean("perm_granted", false))
        private set
    var showLocationDialog by mutableStateOf(false)
        private set
    var vacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            entityExtractor.initialize()
            if (!locationPermissionGranted) {
                showLocationDialog = true
            }
        }
    }

    fun clearSearch() {
        searchQuery = ""
        places = emptyList()
        placesWithStats = emptyList()
        vacations = emptyList()
        errorMessage = null
    }

    fun setUserLocation(lat: Double, lon: Double) {
        userLocation = Pair(lat, lon)
    }

    fun setLocationPermission(granted: Boolean) {
        locationPermissionGranted = granted
        prefs.edit { putBoolean("perm_granted", granted) }
        showLocationDialog = false
    }

    fun dismissLocationDialog() {
        showLocationDialog = false
    }

    private suspend fun detectLocationInQuery(query: String): Boolean {
        val words = query.split(" ").filter { it.isNotBlank() }

        for (word in words) {
            val testWord = word.lowercase().replaceFirstChar { it.uppercase() }
            val annotation = entityExtractor.extractLocation(testWord)
            val isMlAddress = annotation?.entities?.any { it.type == Entity.TYPE_ADDRESS } ?: false

            if (isMlAddress || (word.length >= MIN_WORD_LENGTH && words.size > 1)) {
                return true
            }
        }
        return false
    }

    private fun buildSearchParameters(foundLocationInText: Boolean): Pair<String?, Int?> {
        val shouldUseGPS = locationPermissionGranted && userLocation != null && !foundLocationInText

        val latLong = if (shouldUseGPS) "${userLocation!!.first},${userLocation!!.second}" else null
        val radius = if (shouldUseGPS) DEFAULT_SEARCH_RADIUS else null

        return Pair(latLong, radius)
    }

    private fun mapExceptionToErrorResource(exception: Throwable): Int {
        val msg = exception.message ?: ""
        return when {
            msg.contains(TOO_MANY_REQUESTS.toString()) -> R.string.error_api_limit
            msg.contains(UNAUTHORIZED.toString()) -> R.string.error_unauthorized
            msg.contains(NETWORK_ERROR_HOST) -> R.string.error_no_internet
            else -> R.string.error_unknown
        }
    }

    fun searchForPlaces(query: String) {
        searchQuery = query

        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            places = emptyList()
            placesWithStats = emptyList()
            vacations = emptyList()
            errorMessage = null
            return
        }

        if (searchCache.containsKey(cleanQuery)) {
            placesWithStats = searchCache[cleanQuery]!!
            places = placesWithStats.map { it.place }
            errorMessage = null
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            Log.d(TAG, "Starting search for: $cleanQuery")

            val foundLocationInText = detectLocationInQuery(cleanQuery)

            val (latLong, radius) = buildSearchParameters(foundLocationInText)

            Log.d(TAG, "Query: $cleanQuery  Location Detected: $foundLocationInText GPS Active: ${latLong != null}")

            val placesResult = searchPlacesUseCase(cleanQuery, latLong = latLong, radius = radius)

            Log.d(TAG, "Searching vacations...")
            val vacationsResult = searchVacationsUseCase(cleanQuery)

            placesResult.onSuccess { list ->
                places = list
                Log.d(TAG, "Found ${list.size} places")

                val stats = list.map { place ->
                    val (rating, count) = getUserCommentsStatsUseCase(place.id).getOrNull()
                        ?: Pair(null, 0)
                    PlaceWithStats(place, rating, count)
                }

                placesWithStats = stats
                searchCache[cleanQuery] = stats
            }.onFailure { exception ->
                Log.e(TAG, "Error searching places: ${exception.message}", exception)
                errorMessage = resourceProvider.getString(mapExceptionToErrorResource(exception))
            }

            vacationsResult.onSuccess { list ->
                vacations = list
                Log.d(TAG, "Found ${list.size} vacations: ${list.map { it.title }}")
            }.onFailure { exception ->
                Log.e(TAG, "Error searching vacations: ${exception.message}", exception)
            }

            if (places.isEmpty() && vacations.isEmpty()) {
                errorMessage = resourceProvider.getString(R.string.error_no_results_found)
            }

            isLoading = false
        }
    }

    class Factory(
        private val context: Context,
        private val repository: PlacesRepositoryImpl,
        private val vacationsRepository: VacationsRepositoryImpl,
        private val entityExtractor: LocationEntityExtractor,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(
                    SearchPlacesUseCase(repository),
                    GetUserCommentsStatsUseCase(repository),
                    SearchVacationsUseCase(vacationsRepository),
                    entityExtractor,
                    resourceProvider,
                    context.applicationContext
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}