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
import com.planzy.app.domain.usecase.place.AddUserCommentUseCase
import com.planzy.app.domain.usecase.place.GetPlaceDetailsUseCase
import com.planzy.app.domain.usecase.place.GetPlaceReviewsUseCase
import com.planzy.app.domain.usecase.place.GetUserCommentsUseCase
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase,
    private val getPlaceReviewsUseCase: GetPlaceReviewsUseCase,
    private val getUserCommentsUseCase: GetUserCommentsUseCase,
    private val addUserCommentUseCase: AddUserCommentUseCase,
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
            getPlaceDetailsUseCase(locationId)
                .onSuccess { place = it }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    private fun loadTripadvisorReviews() {
        viewModelScope.launch {
            isLoadingReviews = true
            getPlaceReviewsUseCase(locationId, limit = 5)
                .onSuccess { reviews = it }
            isLoadingReviews = false
        }
    }

    fun loadUserComments() {
        viewModelScope.launch {
            isLoadingUserComments = true
            userCommentsErrorMessage = null
            getUserCommentsUseCase(locationId)
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

            addUserCommentUseCase(locationId, text, rating)
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

    class Factory(
        private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase,
        private val getPlaceReviewsUseCase: GetPlaceReviewsUseCase,
        private val getUserCommentsUseCase: GetUserCommentsUseCase,
        private val addUserCommentUseCase: AddUserCommentUseCase,
        private val resourceProvider: ResourceProvider,
        private val locationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaceDetailsViewModel(
                getPlaceDetailsUseCase,
                getPlaceReviewsUseCase,
                getUserCommentsUseCase,
                addUserCommentUseCase,
                resourceProvider,
                locationId
            ) as T
        }
    }
}