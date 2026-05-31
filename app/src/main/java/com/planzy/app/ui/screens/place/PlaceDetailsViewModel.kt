package com.planzy.app.ui.screens.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.usecase.place.GetPlaceDataUseCase
import com.planzy.app.domain.usecase.place.ManagePlaceCommentsUseCase
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val getPlaceDataUseCase: GetPlaceDataUseCase,
    private val managePlaceCommentsUseCase: ManagePlaceCommentsUseCase,
    private val resourceProvider: ResourceProvider,
    private val locationId: String
) : ViewModel() {

    var place by mutableStateOf<Place?>(null)
        private set

    var reviews by mutableStateOf<List<PlaceReview>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingReviews by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userComments by mutableStateOf<List<UserComment>>(emptyList())
        private set

    var isLoadingUserComments by mutableStateOf(false)
        private set

    var userCommentsErrorMessage by mutableStateOf<String?>(null)
        private set

    var isSubmittingComment by mutableStateOf(false)
        private set

    var commentErrorMessage by mutableStateOf<String?>(null)
        private set

    var isDeletingComment by mutableStateOf(false)
        private set

    var isUpdatingComment by mutableStateOf(false)
        private set

    init {
        loadAllData()
    }

    fun onRetry() {
        loadAllData()
    }

    private fun loadAllData() {
        loadPlaceDetails()
        loadTripadvisorReviews()
        loadUserComments()
    }

    private fun loadPlaceDetails() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            getPlaceDataUseCase.getPlaceDetails(locationId)
                .onSuccess { place = it }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    private fun loadTripadvisorReviews() {
        viewModelScope.launch {
            isLoadingReviews = true
            getPlaceDataUseCase.getPlaceReviews(locationId, limit = 5)
                .onSuccess { reviews = it }
            isLoadingReviews = false
        }
    }

    fun loadUserComments() {
        viewModelScope.launch {
            isLoadingUserComments = true
            userCommentsErrorMessage = null
            getPlaceDataUseCase.getUserComments(locationId)
                .onSuccess {
                    userComments = it
                    isLoadingUserComments = false
                }
                .onFailure {
                    userCommentsErrorMessage = resourceProvider.getString(R.string.error_loading_community_comments)
                    isLoadingUserComments = false
                }
        }
    }

    fun addUserComment(text: String, rating: Int) {
        viewModelScope.launch {
            isSubmittingComment = true
            commentErrorMessage = null

            managePlaceCommentsUseCase.addComment(locationId, text, rating)
                .onSuccess { newComment ->
                    userComments = listOf(newComment) + userComments
                    isSubmittingComment = false
                }
                .onFailure { error ->
                    commentErrorMessage = error.message
                    isSubmittingComment = false
                }
        }
    }

    fun updateUserComment(commentId: String, text: String, rating: Int) {
        viewModelScope.launch {
            isUpdatingComment = true
            commentErrorMessage = null

            managePlaceCommentsUseCase.updateComment(commentId, text, rating)
                .onSuccess {
                    loadUserComments()
                    isUpdatingComment = false
                }
                .onFailure { error ->
                    commentErrorMessage = error.message
                    isUpdatingComment = false
                }
        }
    }

    fun deleteUserComment(commentId: String) {
        viewModelScope.launch {
            isDeletingComment = true

            managePlaceCommentsUseCase.deleteComment(commentId)
                .onSuccess {
                    userComments = userComments.filter { it.id != commentId }
                    isDeletingComment = false
                }
                .onFailure {
                    isDeletingComment = false
                }
        }
    }

    class Factory(
        private val getPlaceDataUseCase: GetPlaceDataUseCase,
        private val managePlaceCommentsUseCase: ManagePlaceCommentsUseCase,
        private val resourceProvider: ResourceProvider,
        private val locationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaceDetailsViewModel(
                getPlaceDataUseCase,
                managePlaceCommentsUseCase,
                resourceProvider,
                locationId
            ) as T
        }
    }
}