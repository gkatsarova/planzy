package com.planzy.app.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.usecase.api.SearchPlacesUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val searchPlacesUseCase: SearchPlacesUseCase
) : ViewModel() {
    var places by mutableStateOf<List<Place>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun searchForPlaces(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            val result = searchPlacesUseCase(destination = query)
            result.onSuccess { foundPlaces ->
                places = foundPlaces
            }.onFailure {
                places = emptyList()
            }
            isLoading = false
        }
    }

    class Factory(
        private val repository: PlacesRepositoryImpl
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(
                    SearchPlacesUseCase(repository)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}