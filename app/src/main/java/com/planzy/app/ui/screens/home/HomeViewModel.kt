package com.planzy.app.ui.screens.home

import android.content.Context
import android.util.Log
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

class HomeViewModel(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val entityExtractor: LocationEntityExtractor,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE)

    var places by mutableStateOf<List<Place>>(emptyList())
    var isLoading by mutableStateOf(false)
    var userLocation by mutableStateOf<Pair<Double, Double>?>(null)

    var locationPermissionGranted by mutableStateOf(prefs.getBoolean("perm_granted", false))
    var showLocationDialog by mutableStateOf(false)

    init {
        viewModelScope.launch {
            entityExtractor.initialize()
            if (!locationPermissionGranted) {
                showLocationDialog = true
            }
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

    fun searchForPlaces(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            isLoading = true

            val hasCityPattern = containsCityPattern(query)

            val isGeneric = isGenericQuery(query)

            val extracted = entityExtractor.extractLocation(query)
            val mlKitDetectedCity = extracted != null

            val shouldUseGPS = locationPermissionGranted &&
                    userLocation != null &&
                    isGeneric &&
                    !hasCityPattern &&
                    !mlKitDetectedCity

            Log.d("SearchFlow", """
                Query: '$query'
                Generic: $isGeneric
                City pattern: $hasCityPattern
                ML Kit city: ${extracted?.locationText}
                Use GPS: $shouldUseGPS
                GPS coords: $userLocation
            """.trimIndent())

            val result = searchPlacesUseCase(
                destination = query,
                latLong = if (shouldUseGPS) "${userLocation!!.first},${userLocation!!.second}" else null,
                radius = if (shouldUseGPS) 25 else null
            )

            result.onSuccess {
                places = it
                Log.d("SearchFlow", "Found ${it.size} places")
            }
            result.onFailure {
                Log.e("SearchFlow", "Search failed", it)
            }

            isLoading = false
        }
    }

    private fun containsCityPattern(query: String): Boolean {
        val words = query.trim().split(Regex("\\s+"))

        if (words.size >= 2) {
            val genericTerms = listOf(
                "restaurant", "hotel", "cafe", "park", "museum",
                "bar", "pizza", "food", "coffee", "club", "pub",
                "gym", "shop", "store", "mall", "cinema", "theatre", "sushi"
            )

            val hasGenericTerm = words.drop(1).any { word ->
                genericTerms.any { it.equals(word, ignoreCase = true) }
            }

            if (hasGenericTerm) {
                return true
            }
        }

        return false
    }

    private fun isGenericQuery(query: String): Boolean {
        val genericTerms = listOf(
            "restaurant", "hotel", "cafe", "park", "museum",
            "bar", "pizza", "food", "coffee", "club", "pub",
            "gym", "shop", "store", "mall", "cinema", "theatre", "sushi"
        )
        val lowerQuery = query.lowercase()
        return genericTerms.any { lowerQuery.contains(it) }
    }

    fun setUserLocation(lat: Double, lng: Double) {
        userLocation = lat to lng
    }

    class Factory(
        private val context: Context,
        private val r: PlacesRepositoryImpl,
        private val e: LocationEntityExtractor
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(SearchPlacesUseCase(r), e, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}