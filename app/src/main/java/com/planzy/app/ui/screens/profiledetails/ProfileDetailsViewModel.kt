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
import com.planzy.app.domain.usecase.follow.FollowUserUseCase
import com.planzy.app.domain.usecase.follow.GetFollowStatsUseCase
import com.planzy.app.domain.usecase.follow.GetFollowersUseCase
import com.planzy.app.domain.usecase.follow.GetFollowingUseCase
import com.planzy.app.domain.usecase.follow.UnfollowUserUseCase
import com.planzy.app.domain.usecase.user.GetUserByUsernameUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsByIdUseCase
import kotlinx.coroutines.launch

sealed interface UserState {
    data object Loading : UserState
    data class Success(val user: User) : UserState
    data class Error(val message: String) : UserState
}

class ProfileDetailsViewModel(
    private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
    private val getUserVacationsByIdUseCase: GetUserVacationsByIdUseCase,
    private val getFollowStatsUseCase: GetFollowStatsUseCase,
    private val getFollowersUseCase: GetFollowersUseCase,
    private val getFollowingUseCase: GetFollowingUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var userState by mutableStateOf<UserState>(UserState.Loading)
        private set

    var vacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var isLoadingVacations by mutableStateOf(false)
        private set

    var vacationsError by mutableStateOf<String?>(null)
        private set

    var followStats by mutableStateOf<FollowStats?>(null)
        private set

    var isLoadingFollowStats by mutableStateOf(false)
        private set

    var isToggleFollowLoading by mutableStateOf(false)
        private set

    var followError by mutableStateOf<String?>(null)
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

    var loggedInUserId by mutableStateOf<String?>(null)
        private set

    init {
        fetchLoggedInUser()
    }

    private fun fetchLoggedInUser() {
        viewModelScope.launch {
            loggedInUserId = getCurrentUserUseCase()?.id
        }
    }

    fun loadUserByUsername(username: String) {
        viewModelScope.launch {
            userState = UserState.Loading

            getUserByUsernameUseCase(username)
                .onSuccess { loadedUser ->
                    if (loadedUser == null) {
                        userState = UserState.Error(
                            resourceProvider.getString(R.string.user_not_found)
                        )
                    } else {
                        userState = UserState.Success(loadedUser)
                        loadUserVacations(loadedUser.auth_id)
                        loadFollowStats(loadedUser.auth_id)
                    }
                }
                .onFailure { exception ->
                    userState = UserState.Error(
                        resourceProvider.getString(R.string.error_loading_user)
                    )
                }
        }
    }

    private fun loadUserVacations(userId: String) {
        viewModelScope.launch {
            isLoadingVacations = true
            vacationsError = null

            getUserVacationsByIdUseCase(userId)
                .onSuccess { userVacations ->
                    vacations = userVacations
                }
                .onFailure { exception ->
                    vacationsError = resourceProvider.getString(R.string.error_loading_vacations)
                }

            isLoadingVacations = false
        }
    }

    private fun loadFollowStats(userId: String) {
        viewModelScope.launch {
            isLoadingFollowStats = true
            followError = null

            getFollowStatsUseCase(userId)
                .onSuccess { stats ->
                    followStats = stats
                }
                .onFailure { exception ->
                    followError = resourceProvider.getString(R.string.error_loading_follow_stats)
                }

            isLoadingFollowStats = false
        }
    }

    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            isLoadingFollowers = true
            followersError = null

            getFollowersUseCase(userId)
                .onSuccess { followersList ->
                    followers = followersList
                }
                .onFailure { exception ->
                    followersError = resourceProvider.getString(R.string.error_loading_followers)
                }

            isLoadingFollowers = false
        }
    }

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            isLoadingFollowing = true
            followingError = null

            getFollowingUseCase(userId)
                .onSuccess { followingList ->
                    following = followingList
                }
                .onFailure { exception ->
                    followingError = resourceProvider.getString(R.string.error_loading_following)
                }

            isLoadingFollowing = false
        }
    }

    fun toggleFollow() {
        val currentUser = when (val state = userState) {
            is UserState.Success -> state.user
            else -> return
        }
        val currentStats = followStats ?: return

        viewModelScope.launch {
            isToggleFollowLoading = true
            followError = null

            val result = if (currentStats.isFollowing) {
                unfollowUserUseCase(currentUser.auth_id)
            } else {
                followUserUseCase(currentUser.auth_id)
            }

            result
                .onSuccess {
                    followStats = followStats?.copy(
                        isFollowing = !currentStats.isFollowing,
                        followersCount = if (!currentStats.isFollowing) {
                            currentStats.followersCount + 1
                        } else {
                            currentStats.followersCount - 1
                        }
                    )
                }
                .onFailure { exception ->
                    followError = resourceProvider.getString(R.string.error_updating_follow_status)
                }

            isToggleFollowLoading = false
        }
    }

    fun refreshFollowStats() {
        val userId = when (val state = userState) {
            is UserState.Success -> state.user.auth_id
            else -> return
        }
        loadFollowStats(userId)
    }

    class Factory(
        private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
        private val getUserVacationsByIdUseCase: GetUserVacationsByIdUseCase,
        private val getFollowStatsUseCase: GetFollowStatsUseCase,
        private val getFollowersUseCase: GetFollowersUseCase,
        private val getFollowingUseCase: GetFollowingUseCase,
        private val followUserUseCase: FollowUserUseCase,
        private val unfollowUserUseCase: UnfollowUserUseCase,
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileDetailsViewModel::class.java)) {
                return ProfileDetailsViewModel(
                    getUserByUsernameUseCase,
                    getUserVacationsByIdUseCase,
                    getFollowStatsUseCase,
                    getFollowersUseCase,
                    getFollowingUseCase,
                    followUserUseCase,
                    unfollowUserUseCase,
                    getCurrentUserUseCase,
                    resourceProvider
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}