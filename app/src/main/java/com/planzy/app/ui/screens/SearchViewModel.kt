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
import com.planzy.app.domain.usecase.api.SearchPlacesUseCase
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.google.mlkit.nl.entityextraction.Entity

class SearchViewModel(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val entityExtractor: LocationEntityExtractor,
    private val resourceProvider: ResourceProvider,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE)
    private val searchCache = mutableMapOf<String, List<Place>>()

    var places by mutableStateOf<List<Place>>(emptyList())
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

    init {
        viewModelScope.launch {
            entityExtractor.initialize()
            if (!locationPermissionGranted) {
                showLocationDialog = true
            }
        }
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

    fun searchForPlaces(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            places = emptyList()
            errorMessage = null
            return
        }

        if (searchCache.containsKey(cleanQuery)) {
            places = searchCache[cleanQuery]!!
            errorMessage = null
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val words = cleanQuery.split(" ").filter { it.isNotBlank() }
            var foundLocationInText = false

            for (word in words) {
                val testWord = word.lowercase().replaceFirstChar { it.uppercase() }
                val annotation = entityExtractor.extractLocation(testWord)

                val isMlAddress = annotation?.entities?.any { it.type == Entity.TYPE_ADDRESS } ?: false

                if (isMlAddress || (word.length >= 4 && words.size > 1)) {
                    foundLocationInText = true
                    break
                }
            }

            val shouldUseGPS = locationPermissionGranted && userLocation != null && !foundLocationInText

            Log.d("PlanzySearch", "Query: $cleanQuery  Location Detected: $foundLocationInText GPS Active: $shouldUseGPS")

            val latLong = if (shouldUseGPS) "${userLocation!!.first},${userLocation!!.second}" else null
            val radius = if (shouldUseGPS) 25 else null

            val result = searchPlacesUseCase(cleanQuery, latLong = latLong, radius = radius)

            result.onSuccess { list ->
                places = list
                searchCache[cleanQuery] = list
                if (list.isEmpty()) {
                    errorMessage = resourceProvider.getString(R.string.error_no_places_found)
                }
            }.onFailure { exception ->
                val msg = exception.message ?: ""
                val resId = when {
                    msg.contains(resourceProvider.getString(R.string.api_error_429)) -> R.string.error_api_limit
                    msg.contains(resourceProvider.getString(R.string.api_error_401)) -> R.string.error_unauthorized
                    msg.contains(resourceProvider.getString(R.string.unable_to_resolve_host)) -> R.string.error_no_internet
                    else -> R.string.error_unknown
                }
                errorMessage = resourceProvider.getString(resId)
            }

            isLoading = false
        }
    }

    class Factory(
        private val context: Context,
        private val repository: PlacesRepositoryImpl,
        private val entityExtractor: LocationEntityExtractor,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(
                    SearchPlacesUseCase(repository),
                    entityExtractor,
                    resourceProvider,
                    context.applicationContext
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}