package com.planzy.app.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.model.User
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.place.GetUserCommentsStatsUseCase
import com.planzy.app.domain.usecase.search.SearchAllOutcome
import com.planzy.app.domain.usecase.search.SearchAllParams
import com.planzy.app.domain.usecase.search.SearchAllUseCase
import com.planzy.app.domain.usecase.vacation.GetVacationCommentsCountUseCase
import kotlinx.coroutines.launch
import androidx.core.content.edit

data class PlaceWithStats(
    val place: Place,
    val userRating: Double?,
    val userReviewsCount: Int
)

class SearchViewModel(
    private val searchAllUseCase: SearchAllUseCase,
    private val getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase,
    private val getVacationCommentsCountUseCase: GetVacationCommentsCountUseCase,
    private val entityExtractor: LocationEntityExtractor,
    context: Context
) : ViewModel() {

    companion object {
        private val TAG = SearchViewModel::class.java.simpleName
    }

    private val prefs = context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE)

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
    var isSearchBarFocused by mutableStateOf(false)
        private set
    var users by mutableStateOf<List<User>>(emptyList())
        private set

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            entityExtractor.initialize()
            if (!locationPermissionGranted) {
                showLocationDialog = true
            }
        }
    }

    fun updateSearchBarFocus(isFocused: Boolean) {
        isSearchBarFocused = isFocused
    }

    fun clearSearch() {
        searchQuery = ""
        places = emptyList()
        placesWithStats = emptyList()
        vacations = emptyList()
        users = emptyList()
        errorMessage = null
        isSearchBarFocused = false
        searchJob?.cancel()
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

    fun updateQuery(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            places = emptyList()
            placesWithStats = emptyList()
            vacations = emptyList()
            users = emptyList()
            errorMessage = null
            searchJob?.cancel()
        }
    }
    fun submitSearch() {
        val cleanQuery = searchQuery.trim()
        if (cleanQuery.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            isLoading = true
            errorMessage = null
            Log.d(TAG, "Starting search for: $cleanQuery")

            val params = SearchAllParams(
                query = cleanQuery,
                userLocation = userLocation,
                locationPermissionGranted = locationPermissionGranted
            )

            when (val outcome = searchAllUseCase(params)) {
                is SearchAllOutcome.Success -> applyResult(outcome.result)

                is SearchAllOutcome.PlacesError -> {
                    Log.w(TAG, "Partial result: ${outcome.message}")
                    applyResult(outcome.partialResult)
                }

                is SearchAllOutcome.Empty -> {
                    places = emptyList()
                    placesWithStats = emptyList()
                    vacations = emptyList()
                    users = emptyList()
                    errorMessage = outcome.message
                }
            }

            isLoading = false
        }
    }

    fun search(query: String) {
        updateQuery(query)
        if (query.isNotBlank()) submitSearch()
    }
    private suspend fun applyResult(result: com.planzy.app.domain.usecase.search.SearchAllResult) {
        users = result.users
        vacations = enrichVacationsWithComments(result.vacations)
        val enriched = enrichPlacesWithStats(result.places)
        placesWithStats = enriched
        places = enriched.map { it.place }
    }

    private suspend fun enrichVacationsWithComments(list: List<Vacation>): List<Vacation> =
        list.map { vacation ->
            val count = getVacationCommentsCountUseCase(vacation.id).getOrDefault(0)
            vacation.copy(commentsCount = count)
        }

    private suspend fun enrichPlacesWithStats(list: List<Place>): List<PlaceWithStats> =
        list.map { place ->
            val (rating, count) = getUserCommentsStatsUseCase(place.id).getOrNull()
                ?: Pair(null, 0)
            PlaceWithStats(place, rating, count)
        }

    class Factory(
        private val context: Context,
        private val repository: PlacesRepositoryImpl,
        private val userRepository: UserRepositoryImpl,
        private val vacationsRepository: VacationsRepositoryImpl,
        private val entityExtractor: LocationEntityExtractor,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(
                    searchAllUseCase = SearchAllUseCase(
                        placesRepository = repository,
                        vacationsRepository = vacationsRepository,
                        userRepository = userRepository,
                        entityExtractor = entityExtractor,
                        resourceProvider = resourceProvider
                    ),
                    getUserCommentsStatsUseCase = GetUserCommentsStatsUseCase(repository),
                    getVacationCommentsCountUseCase = GetVacationCommentsCountUseCase(
                        vacationsRepository
                    ),
                    entityExtractor = entityExtractor,
                    context = context.applicationContext
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}