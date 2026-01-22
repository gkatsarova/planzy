package com.planzy.app.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.usecase.auth.DeleteAccountUseCase
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.auth.SignOutUseCase
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

                    getUserByAuthIdUseCase(authId)
                        .onSuccess { user ->
                            if (user != null) {
                                username = user.username
                                email = user.email
                                profilePictureUrl = user.profilePictureUrl
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

    fun signOut() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            signOutUseCase()
                .onSuccess {
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
                resourceProvider
            ) as T
        }
    }
}