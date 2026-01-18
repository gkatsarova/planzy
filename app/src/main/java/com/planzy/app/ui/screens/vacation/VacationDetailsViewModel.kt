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
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.vacation.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class VacationDetailsViewModel(
    private val getVacationDetailsUseCase: GetVacationDetailsUseCase,
    private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
    private val getVacationCommentsUseCase: GetVacationCommentsUseCase,
    private val addVacationCommentUseCase: AddVacationCommentUseCase,
    private val updateVacationCommentUseCase: UpdateVacationCommentUseCase,
    private val deleteVacationCommentUseCase: DeleteVacationCommentUseCase,
    private val placesRepository: PlacesRepository,
    private val resourceProvider: ResourceProvider,
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

    var vacationComments by mutableStateOf<List<VacationComment>>(emptyList())
        private set

    var isLoadingComments by mutableStateOf(false)
        private set

    var commentsErrorMessage by mutableStateOf<String?>(null)
        private set

    var isSubmittingComment by mutableStateOf(false)
        private set

    var commentErrorMessage by mutableStateOf<String?>(null)
        private set

    var isDeletingComment by mutableStateOf(false)
        private set

    var isUpdatingComment by mutableStateOf(false)
        private set

    private var userRatingsCache = mutableMapOf<String, Pair<Double?, Int>>()

    init {
        loadVacationDetails()
        loadVacationComments()
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
                    errorMessage = exception.message ?: resourceProvider.getString(R.string.unknown_error)
                    isLoading = false
                }
        }
    }

    private suspend fun loadUserRatingsForPlaces(places: List<Place>) {
        places.map { place ->
            viewModelScope.async {
                placesRepository.getUserCommentsStats(place.id)
                    .onSuccess { (rating, count) ->
                        userRatingsCache[place.id] = Pair(rating, count)
                    }
                    .onFailure {
                        userRatingsCache[place.id] = Pair(null, 0)
                    }
            }
        }.awaitAll()
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

    fun loadVacationComments() {
        viewModelScope.launch {
            isLoadingComments = true
            commentsErrorMessage = null
            getVacationCommentsUseCase(vacationId)
                .onSuccess {
                    vacationComments = it
                    isLoadingComments = false
                }
                .onFailure {
                    commentsErrorMessage = resourceProvider.getString(R.string.error_loading_community_comments)
                    isLoadingComments = false
                }
        }
    }

    fun addVacationComment(text: String) {
        viewModelScope.launch {
            isSubmittingComment = true
            commentErrorMessage = null

            addVacationCommentUseCase(vacationId, text)
                .onSuccess { newComment ->
                    vacationComments = listOf(newComment) + vacationComments
                    isSubmittingComment = false
                }
                .onFailure { error ->
                    commentErrorMessage = error.message
                    isSubmittingComment = false
                }
        }
    }

    fun updateVacationComment(commentId: String, text: String) {
        viewModelScope.launch {
            isUpdatingComment = true
            commentErrorMessage = null

            updateVacationCommentUseCase(commentId, text)
                .onSuccess {
                    loadVacationComments()
                    isUpdatingComment = false
                }
                .onFailure { error ->
                    commentErrorMessage = error.message
                    isUpdatingComment = false
                }
        }
    }

    fun deleteVacationComment(commentId: String) {
        viewModelScope.launch {
            isDeletingComment = true

            deleteVacationCommentUseCase(commentId)
                .onSuccess {
                    vacationComments = vacationComments.filter { it.id != commentId }
                    isDeletingComment = false
                }
                .onFailure {
                    isDeletingComment = false
                }
        }
    }

    fun onRetry() {
        loadVacationDetails()
        loadVacationComments()
    }

    class Factory(
        private val getVacationDetailsUseCase: GetVacationDetailsUseCase,
        private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
        private val getVacationCommentsUseCase: GetVacationCommentsUseCase,
        private val addVacationCommentUseCase: AddVacationCommentUseCase,
        private val updateVacationCommentUseCase: UpdateVacationCommentUseCase,
        private val deleteVacationCommentUseCase: DeleteVacationCommentUseCase,
        private val placesRepository: PlacesRepository,
        private val resourceProvider: ResourceProvider,
        private val vacationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VacationDetailsViewModel(
                getVacationDetailsUseCase,
                removePlaceFromVacationUseCase,
                getVacationCommentsUseCase,
                addVacationCommentUseCase,
                updateVacationCommentUseCase,
                deleteVacationCommentUseCase,
                placesRepository,
                resourceProvider,
                vacationId
            ) as T
        }
    }
}