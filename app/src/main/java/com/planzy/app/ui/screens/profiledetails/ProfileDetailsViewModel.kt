package com.planzy.app.ui.screens.profiledetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.FollowStats
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.follow.GetFollowDataUseCase
import com.planzy.app.domain.usecase.follow.ManageFollowUseCase
import com.planzy.app.domain.usecase.user.GetUserByUsernameUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsByIdUseCase
import kotlinx.coroutines.launch

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}

class ProfileDetailsViewModel(
    private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
    private val getUserVacationsByIdUseCase: GetUserVacationsByIdUseCase,
    private val getFollowDataUseCase: GetFollowDataUseCase,
    private val manageFollowUseCase: ManageFollowUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var userState by mutableStateOf<UserState>(UserState.Loading)
        private set

    var loggedInUserId by mutableStateOf("")
        private set

    var followStats by mutableStateOf<FollowStats?>(null)
        private set

    var isLoadingFollowStats by mutableStateOf(false)
        private set

    var isToggleFollowLoading by mutableStateOf(false)
        private set

    var followers by mutableStateOf<List<User>>(emptyList())
        private set

    var following by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoadingFollowers by mutableStateOf(false)
        private set

    var isLoadingFollowing by mutableStateOf(false)
        private set

    var followersError by mutableStateOf<String?>(null)
        private set

    var followingError by mutableStateOf<String?>(null)
        private set

    var vacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var isLoadingVacations by mutableStateOf(false)
        private set

    var vacationsError by mutableStateOf<String?>(null)
        private set

    init {
        loadLoggedInUser()
    }

    private fun loadLoggedInUser() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                loggedInUserId = currentUser.id
            }
        }
    }

    fun loadUserByUsername(username: String) {
        viewModelScope.launch {
            userState = UserState.Loading

            getUserByUsernameUseCase(username)
                .onSuccess { user ->
                    if (user != null) {
                        userState = UserState.Success(user)
                        loadVacations(user.auth_id)
                        loadFollowData(user.auth_id)
                    } else {
                        userState = UserState.Error(resourceProvider.getString(R.string.error_unknown))
                    }
                }
                .onFailure {
                    userState = UserState.Error(it.message ?: resourceProvider.getString(R.string.error_unknown))
                }
        }
    }

    private fun loadFollowData(authId: String) {
        viewModelScope.launch {
            isLoadingFollowStats = true
            isLoadingFollowers = true
            isLoadingFollowing = true
            followersError = null
            followingError = null

            getFollowDataUseCase(authId)
                .onSuccess { data ->
                    followStats = FollowStats(
                        isFollowing = data.isFollowing,
                        followersCount = data.followersCount,
                        followingCount = data.followingCount
                    )
                    followers = data.followers
                    following = data.following
                }
                .onFailure {
                    followersError = resourceProvider.getString(R.string.error_loading_followers)
                    followingError = resourceProvider.getString(R.string.error_loading_following)
                }

            isLoadingFollowStats = false
            isLoadingFollowers = false
            isLoadingFollowing = false
        }
    }

    private fun loadVacations(userId: String) {
        viewModelScope.launch {
            isLoadingVacations = true
            vacationsError = null
            getUserVacationsByIdUseCase(userId)
                .onSuccess { vacationsList ->
                    vacations = vacationsList
                }
                .onFailure {
                    vacationsError = it.message
                }
            isLoadingVacations = false
        }
    }

    fun refreshFollowStats() {
        val successState = userState as? UserState.Success ?: return
        loadFollowData(successState.user.auth_id)
    }

    fun loadFollowers(authId: String) {
        loadFollowData(authId)
    }

    fun loadFollowing(authId: String) {
        loadFollowData(authId)
    }

    fun toggleFollow() {
        val successState = userState as? UserState.Success ?: return
        val currentStats = followStats ?: return

        viewModelScope.launch {
            isToggleFollowLoading = true
            manageFollowUseCase(successState.user.auth_id, currentStats)
                .onSuccess { updatedStats ->
                    followStats = updatedStats
                    loadFollowData(successState.user.auth_id)
                }
            isToggleFollowLoading = false
        }
    }

    class Factory(
        private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
        private val getUserVacationsByIdUseCase: GetUserVacationsByIdUseCase,
        private val getFollowDataUseCase: GetFollowDataUseCase,
        private val manageFollowUseCase: ManageFollowUseCase,
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileDetailsViewModel(
                getUserByUsernameUseCase,
                getUserVacationsByIdUseCase,
                getFollowDataUseCase,
                manageFollowUseCase,
                getCurrentUserUseCase,
                resourceProvider
            ) as T
        }
    }
}