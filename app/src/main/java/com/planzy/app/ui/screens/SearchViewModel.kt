package com.planzy.app.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.usecase.api.SearchPlacesUseCase
import kotlinx.coroutines.launch
import androidx.core.content.edit

class SearchViewModel(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val entityExtractor: LocationEntityExtractor,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE)
    private val searchCache = mutableMapOf<String, List<Place>>()
    private var lastSearchTime = 0L

    var places by mutableStateOf<List<Place>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var userLocation by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var locationPermissionGranted by mutableStateOf(prefs.getBoolean("perm_granted", false))
        private set

    var showLocationDialog by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            entityExtractor.initialize()
            if (!locationPermissionGranted) showLocationDialog = true
        }
    }

    fun dismissLocationDialog() {
        showLocationDialog = false
    }

    fun setLocationPermission(granted: Boolean) {
        locationPermissionGranted = granted
        prefs.edit { putBoolean("perm_granted", granted) }
        showLocationDialog = false
    }

    fun setUserLocation(lat: Double, lng: Double) {
        userLocation = lat to lng
    }

    fun searchForPlaces(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            places = emptyList()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastSearchTime < 1000) return
        lastSearchTime = now

        searchCache[cleanQuery]?.let {
            places = it
            return
        }

        viewModelScope.launch {
            isLoading = true

            val extracted = entityExtractor.extractLocation(cleanQuery)
            val words = cleanQuery.lowercase().split(" ")
            val potentialCities = words.filter { it.length > 3 }.take(2)
            val detectedCity = extracted?.locationText ?: potentialCities.firstOrNull()

            val locationPosition = if (detectedCity != null) {
                getLocationPosition(cleanQuery, detectedCity)
            } else LocationPosition.NONE

            val hasExplicitLocation = detectedCity != null &&
                    (locationPosition == LocationPosition.START || locationPosition == LocationPosition.MIDDLE)

            val hasPermission = locationPermissionGranted
            val hasUserLocation = userLocation != null
            val noCityDetected = !hasExplicitLocation

            val shouldUseGPS = hasPermission && hasUserLocation && noCityDetected

            val latLong = if (shouldUseGPS) "${userLocation!!.first},${userLocation!!.second}" else null
            val radius = if (shouldUseGPS) 25 else null

            val result = searchPlacesUseCase(cleanQuery, latLong = latLong, radius = radius)
            result.onSuccess {
                places = it
                searchCache[cleanQuery] = it
            }
            isLoading = false
        }
    }

    private fun getLocationPosition(query: String, locationText: String): LocationPosition {
        val normalizedQuery = query.lowercase()
        val normalizedLocation = locationText.lowercase()
        return when {
            normalizedQuery.startsWith(normalizedLocation) || normalizedQuery.endsWith(normalizedLocation) -> LocationPosition.START
            normalizedQuery.contains(normalizedLocation) -> LocationPosition.MIDDLE
            else -> LocationPosition.NONE
        }
    }

    enum class LocationPosition { NONE, START, MIDDLE }

    class Factory(
        private val context: Context,
        private val repository: PlacesRepositoryImpl,
        private val entityExtractor: LocationEntityExtractor
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(
                    SearchPlacesUseCase(repository),
                    entityExtractor,
                    context
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}