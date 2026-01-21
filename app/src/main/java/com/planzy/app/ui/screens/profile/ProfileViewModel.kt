package com.planzy.app.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase,
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

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val currentUser = getCurrentUserUseCase()

                if (currentUser != null) {
                    val authId = currentUser.id

                    getUserByAuthIdUseCase(authId)
                        .onSuccess { user ->
                            username = user?.username ?: ""
                            email = user?.email ?: ""
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

    fun retry() {
        loadUserProfile()
    }

    class Factory(
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(
                getCurrentUserUseCase,
                getUserByAuthIdUseCase,
                resourceProvider
            ) as T
        }
    }
}