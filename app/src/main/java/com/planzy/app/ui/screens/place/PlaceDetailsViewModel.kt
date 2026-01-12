package com.planzy.app.ui.screens.place

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.usecase.api.GetPlaceDetailsUseCase
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase,
    private val resourceProvider: ResourceProvider,
    private val locationId: String
) : ViewModel() {

    var place by mutableStateOf<Place?>(null)
        private set
    var photos by mutableStateOf<List<String>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPlaceDetails()
    }

    fun loadPlaceDetails() {
        if (locationId.isBlank()) {
            errorMessage = resourceProvider.getString(R.string.error_loading_details)
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            getPlaceDetailsUseCase(locationId)
                .onSuccess { placeData ->
                    place = placeData
                    photos = placeData.photoUrl?.let { listOf(it) } ?: emptyList()
                }
                .onFailure { exception ->
                    errorMessage = resourceProvider.getString(R.string.error_loading_details)
                }

            isLoading = false
        }
    }

    fun onRetry() {
        loadPlaceDetails()
    }

    class Factory(
        private val repository: PlacesRepositoryImpl,
        private val resourceProvider: ResourceProvider,
        private val locationId: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaceDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlaceDetailsViewModel(
                    GetPlaceDetailsUseCase(repository),
                    resourceProvider,
                    locationId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}