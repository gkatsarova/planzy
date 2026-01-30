package com.planzy.app.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.manager.ProfilePictureManager
import com.planzy.app.domain.model.FollowStats
import com.planzy.app.domain.usecase.auth.DeleteAccountUseCase
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.auth.SignOutUseCase
import com.planzy.app.domain.usecase.follow.GetFollowStatsUseCase
import com.planzy.app.domain.usecase.follow.GetFollowersUseCase
import com.planzy.app.domain.usecase.follow.GetFollowingUseCase
import com.planzy.app.domain.usecase.user.DeleteProfilePictureUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.domain.usecase.user.UpdateProfilePictureUseCase
import com.planzy.app.domain.usecase.user.UploadProfilePictureUseCase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val updateProfilePictureUseCase: UpdateProfilePictureUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val getFollowStatsUseCase: GetFollowStatsUseCase,
    private val getFollowersUseCase: GetFollowersUseCase,
    private val getFollowingUseCase: GetFollowingUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var username by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLogoutSuccessful by mutableStateOf(false)
        private set

    var showDeleteConfirmation by mutableStateOf(false)
        private set

    var isDeleteSuccessful by mutableStateOf(false)
        private set

    var isUploadingPicture by mutableStateOf(false)
        private set

    var profilePictureUrl by mutableStateOf<String?>(null)
        private set

    var followStats by mutableStateOf<FollowStats?>(null)
        private set

    var isLoadingFollowStats by mutableStateOf(false)
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

    var currentUserAuthId by mutableStateOf("")
        private set

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                SupabaseClient.client.auth.refreshCurrentSession()
                val currentUser = getCurrentUserUseCase()

                if (currentUser != null) {
                    val authId = currentUser.id
                    currentUserAuthId = authId

                    getUserByAuthIdUseCase(authId)
                        .onSuccess { user ->
                            if (user != null) {
                                username = user.username
                                email = user.email
                                profilePictureUrl = user.profilePictureUrl
                                ProfilePictureManager.updateUrl(user.profilePictureUrl)
                                loadFollowStats(authId)
                            }
                            isLoading = false
                        }
                        .onFailure { exception ->
                            errorMessage = exception.message
                            isLoading = false
                        }
                } else {
                    errorMessage = resourceProvider.getString(R.string.error_user_not_logged_in)
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    private fun loadFollowStats(authId: String) {
        viewModelScope.launch {
            isLoadingFollowStats = true

            getFollowStatsUseCase(authId)
                .onSuccess { stats ->
                    followStats = stats
                }
                .onFailure { exception ->
                    errorMessage = null
                }

            isLoadingFollowStats = false
        }
    }

    fun loadFollowers() {
        viewModelScope.launch {
            isLoadingFollowers = true
            followersError = null

            getFollowersUseCase(currentUserAuthId)
                .onSuccess { followersList ->
                    followers = followersList
                }
                .onFailure { exception ->
                    followersError = resourceProvider.getString(R.string.error_loading_followers)
                }

            isLoadingFollowers = false
        }
    }

    fun loadFollowing() {
        viewModelScope.launch {
            isLoadingFollowing = true
            followingError = null

            getFollowingUseCase(currentUserAuthId)
                .onSuccess { followingList ->
                    following = followingList
                }
                .onFailure { exception ->
                    followingError = resourceProvider.getString(R.string.error_loading_following)
                }

            isLoadingFollowing = false
        }
    }

    fun refreshFollowStats() {
        val currentAuthId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return
        loadFollowStats(currentAuthId)
    }

    fun signOut() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            signOutUseCase()
                .onSuccess {
                    ProfilePictureManager.updateUrl(null)
                    isLoading = false
                    isLogoutSuccessful = true
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isLoading = false
                }
        }
    }

    fun showDeleteDialog() {
        showDeleteConfirmation = true
    }

    fun dismissDeleteDialog() {
        showDeleteConfirmation = false
    }

    fun deleteAccount() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            showDeleteConfirmation = false

            deleteAccountUseCase()
                .onSuccess {
                    ProfilePictureManager.updateUrl(null)
                    isLoading = false
                    isDeleteSuccessful = true
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isLoading = false
                }
        }
    }

    fun uploadProfilePicture(imageFile: File) {
        viewModelScope.launch {
            isUploadingPicture = true
            errorMessage = null

            uploadProfilePictureUseCase(imageFile)
                .onSuccess { url ->
                    updateProfilePictureUseCase(url)
                        .onSuccess {
                            profilePictureUrl = url
                            ProfilePictureManager.updateUrl(url)
                            isUploadingPicture = false
                        }
                        .onFailure { exception ->
                            errorMessage = exception.message
                            isUploadingPicture = false
                        }
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isUploadingPicture = false
                }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            isUploadingPicture = true
            errorMessage = null

            val currentUrl = profilePictureUrl ?: run {
                isUploadingPicture = false
                return@launch
            }

            deleteProfilePictureUseCase(currentUrl)
                .onSuccess {
                    profilePictureUrl = null
                    ProfilePictureManager.updateUrl(null)
                    isUploadingPicture = false
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                    isUploadingPicture = false
                }
        }
    }

    fun retry() {
        loadUserProfile()
    }

    class Factory(
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val deleteAccountUseCase: DeleteAccountUseCase,
        private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
        private val updateProfilePictureUseCase: UpdateProfilePictureUseCase,
        private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
        private val getFollowStatsUseCase: GetFollowStatsUseCase,
        private val getFollowersUseCase: GetFollowersUseCase,
        private val getFollowingUseCase: GetFollowingUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(
                getCurrentUserUseCase,
                getUserByAuthIdUseCase,
                signOutUseCase,
                deleteAccountUseCase,
                uploadProfilePictureUseCase,
                updateProfilePictureUseCase,
                deleteProfilePictureUseCase,
                getFollowStatsUseCase,
                getFollowersUseCase,
                getFollowingUseCase,
                resourceProvider
            ) as T
        }
    }
}