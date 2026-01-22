package com.planzy.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.domain.manager.ProfilePictureManager
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class PlanzyTopBarViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase
) : ViewModel() {
    var profilePictureUrl by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            SupabaseClient.client.auth.awaitInitialization()
            loadUserProfile()

        }

        viewModelScope.launch {
            ProfilePictureManager.profilePictureUrl.collect { newUrl ->
                profilePictureUrl = newUrl
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {

            val currentUser = getCurrentUserUseCase()

            if (currentUser != null) {
                getUserByAuthIdUseCase(currentUser.id).onSuccess { user ->

                    val url = user?.profilePictureUrl
                    profilePictureUrl = url
                    ProfilePictureManager.updateUrl(url)
                }
            }
        }
    }

    class Factory(
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val getUserByAuthIdUseCase: GetUserByAuthIdUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlanzyTopBarViewModel(
                getCurrentUserUseCase,
                getUserByAuthIdUseCase,
            ) as T
        }
    }
}