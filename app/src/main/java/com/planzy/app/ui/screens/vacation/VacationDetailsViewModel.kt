package com.planzy.app.ui.screens.vacation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.place.GetUserCommentsStatsUseCase
import com.planzy.app.domain.usecase.vacation.*
import com.planzy.app.ui.util.toUserMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class VacationDetailsViewModel(
    private val getVacationDataUseCase: GetVacationDataUseCase,
    private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
    private val addVacationCommentUseCase: AddVacationCommentUseCase,
    private val updateVacationCommentUseCase: UpdateVacationCommentUseCase,
    private val deleteVacationCommentUseCase: DeleteVacationCommentUseCase,
    private val manageSavedVacationUseCase: ManageSavedVacationUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase,
    private val isVacationSavedUseCase: IsVacationSavedUseCase,
    private val resourceProvider: ResourceProvider,
    private val vacationId: String,
    private val onCommentsChanged: () -> Unit = {}
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

    var isSaved by mutableStateOf(false)
        private set

    var isSavingInProgress by mutableStateOf(false)
        private set

    private var currentUserId: String? = null
    private var userRatingsCache = mutableMapOf<String, Pair<Double?, Int>>()

    init {
        viewModelScope.launch {
            currentUserId = getCurrentUserUseCase()?.id
            loadVacationDetails()
            checkIfVacationIsSaved()
        }
    }

    fun loadVacationDetails() {
        viewModelScope.launch {
            isLoading = true
            isLoadingComments = true
            errorMessage = null
            commentsErrorMessage = null

            getVacationDataUseCase(vacationId)
                .onSuccess { details ->
                    vacation = details.vacation
                    creatorUsername = details.creatorUsername
                    places = details.places
                    vacationComments = details.vacationComments

                    isOwner = currentUserId != null && currentUserId == details.vacation.userId

                    loadUserRatingsForPlaces(details.places)

                    isLoading = false
                    isLoadingComments = false
                }
                .onFailure { error ->
                    errorMessage = error.toUserMessage(resourceProvider)
                    commentsErrorMessage = resourceProvider.getString(R.string.error_loading_community_comments)
                    isLoading = false
                    isLoadingComments = false
                }
        }
    }

    private suspend fun loadUserRatingsForPlaces(places: List<Place>) {
        places.map { place ->
            viewModelScope.async {
                getUserCommentsStatsUseCase(place.id)
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
                    vacation = vacation?.copy(placesCount = (vacation?.placesCount ?: 1) - 1)
                    userRatingsCache.remove(placeId)
                }
                .onFailure { error ->
                    errorMessage = error.toUserMessage(resourceProvider)
                }
        }
    }

    fun loadVacationComments() {
        loadVacationDetails()
    }

    fun addVacationComment(text: String) {
        viewModelScope.launch {
            isSubmittingComment = true
            commentErrorMessage = null

            addVacationCommentUseCase(vacationId, text)
                .onSuccess { newComment ->
                    vacationComments = listOf(newComment) + vacationComments
                    isSubmittingComment = false
                    onCommentsChanged()
                }
                .onFailure { error ->
                    commentErrorMessage = error.toUserMessage(resourceProvider)
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
                    commentErrorMessage =  error.toUserMessage(resourceProvider)
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
                    onCommentsChanged()
                }
                .onFailure { error ->
                    commentErrorMessage = error.toUserMessage(resourceProvider)
                    isDeletingComment = false
                }
        }
    }

    private fun checkIfVacationIsSaved() {
        viewModelScope.launch {
            isVacationSavedUseCase(vacationId)
                .onSuccess { saved ->
                    isSaved = saved
                }
        }
    }

    fun toggleSaveVacation() {
        viewModelScope.launch {
            isSavingInProgress = true

            manageSavedVacationUseCase(vacationId)
                .onSuccess { nextSavedState ->
                    isSaved = nextSavedState
                }
                .onFailure { error ->
                    errorMessage = error.toUserMessage(resourceProvider)
                }

            isSavingInProgress = false
        }
    }

    fun onRetry() {
        loadVacationDetails()
    }

    class Factory(
        private val getVacationDataUseCase: GetVacationDataUseCase,
        private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
        private val addVacationCommentUseCase: AddVacationCommentUseCase,
        private val updateVacationCommentUseCase: UpdateVacationCommentUseCase,
        private val deleteVacationCommentUseCase: DeleteVacationCommentUseCase,
        private val manageSavedVacationUseCase: ManageSavedVacationUseCase,
        private val getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase,
        private val isVacationSavedUseCase: IsVacationSavedUseCase,
        private val resourceProvider: ResourceProvider,
        private val vacationId: String,
        private val onCommentsChanged: () -> Unit = {}
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val authRepository = AuthRepositoryImpl(resourceProvider as ResourceProviderImpl)
            val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
            return VacationDetailsViewModel(
                getVacationDataUseCase,
                removePlaceFromVacationUseCase,
                addVacationCommentUseCase,
                updateVacationCommentUseCase,
                deleteVacationCommentUseCase,
                manageSavedVacationUseCase,
                getCurrentUserUseCase,
                getUserCommentsStatsUseCase,
                isVacationSavedUseCase,
                resourceProvider,
                vacationId,
                onCommentsChanged
            ) as T
        }
    }
}