package com.planzy.app.ui.screens.place

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.usecase.api.GetPlaceDetailsUseCase
import com.planzy.app.domain.usecase.api.GetPlaceReviewsUseCase
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase,
    private val getPlaceReviewsUseCase: GetPlaceReviewsUseCase,
    private val resourceProvider: ResourceProvider,
    private val locationId: String
) : ViewModel() {

    var place by mutableStateOf<Place?>(null)
        private set
    var photos by mutableStateOf<List<String>>(emptyList())
        private set
    var reviews by mutableStateOf<List<PlaceReview>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isLoadingReviews by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPlaceDetails()
        loadReviews()
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

    private fun loadReviews() {
        viewModelScope.launch {
            isLoadingReviews = true

            getPlaceReviewsUseCase(locationId, limit = 5)
                .onSuccess { reviewsList ->
                    reviews = reviewsList
                }
                .onFailure { exception ->
                }

            isLoadingReviews = false
        }
    }

    fun onRetry() {
        loadPlaceDetails()
        loadReviews()
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
                    GetPlaceReviewsUseCase(repository),
                    resourceProvider,
                    locationId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}