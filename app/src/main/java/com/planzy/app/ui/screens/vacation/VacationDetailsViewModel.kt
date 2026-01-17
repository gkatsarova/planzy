package com.planzy.app.ui.screens.vacation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.vacation.GetVacationDetailsUseCase
import com.planzy.app.domain.usecase.vacation.RemovePlaceFromVacationUseCase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class VacationDetailsViewModel(
    private val getVacationDetailsUseCase: GetVacationDetailsUseCase,
    private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
    private val placesRepository: PlacesRepository,
    private val recourceProvider: ResourceProvider,
    private val vacationId: String
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var vacation by mutableStateOf<Vacation?>(null)
        private set

    var creatorUsername by mutableStateOf<String?>(null)
        private set

    var places by mutableStateOf<List<Place>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isOwner by mutableStateOf(false)
        private set

    private var userRatingsCache = mutableMapOf<String, Pair<Double?, Int>>()

    init {
        loadVacationDetails()
    }

    fun loadVacationDetails() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            getVacationDetailsUseCase(vacationId)
                .onSuccess { details ->
                    vacation = details.vacation
                    creatorUsername = details.creatorUsername
                    places = details.places

                    val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    isOwner = currentUserId == details.vacation.userId

                    loadUserRatingsForPlaces(details.places)

                    isLoading = false
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: recourceProvider.getString(R.string.unknown_error)
                    isLoading = false
                }
        }
    }

    private fun loadUserRatingsForPlaces(places: List<Place>) {
        viewModelScope.launch {
            places.forEach { place ->
                placesRepository.getUserCommentsStats(place.id)
                    .onSuccess { (rating, count) ->
                        userRatingsCache[place.id] = Pair(rating, count)
                    }
                    .onFailure {
                        userRatingsCache[place.id] = Pair(null, 0)
                    }
            }
        }
    }

    fun getUserRating(placeId: String): Pair<Double?, Int> {
        return userRatingsCache[placeId] ?: Pair(null, 0)
    }

    fun removePlaceFromVacation(placeId: String) {
        viewModelScope.launch {
            removePlaceFromVacationUseCase(vacationId, placeId)
                .onSuccess {
                    places = places.filter { it.id != placeId }

                    vacation = vacation?.copy(placesCount = vacation!!.placesCount - 1)

                    userRatingsCache.remove(placeId)
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                }
        }
    }

    fun onRetry() {
        loadVacationDetails()
    }

    class Factory(
        private val getVacationDetailsUseCase: GetVacationDetailsUseCase,
        private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
        private val placesRepository: PlacesRepository,
        private val recourceProvider: ResourceProvider,
        private val vacationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VacationDetailsViewModel(
                getVacationDetailsUseCase,
                removePlaceFromVacationUseCase,
                placesRepository,
                recourceProvider,
                vacationId
            ) as T
        }
    }
}