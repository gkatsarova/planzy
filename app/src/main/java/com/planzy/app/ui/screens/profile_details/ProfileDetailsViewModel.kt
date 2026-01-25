package com.planzy.app.ui.screens.profile_details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.user.GetUserByUsernameUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsByIdUseCase
import kotlinx.coroutines.launch

class ProfileDetailsViewModel(
    private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
    private val getUserVacationsByIdUseCase: GetUserVacationsByIdUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var vacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var isLoadingVacations by mutableStateOf(false)
        private set

    var vacationsError by mutableStateOf<String?>(null)
        private set

    fun loadUserByUsername(username: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            getUserByUsernameUseCase(username)
                .onSuccess { loadedUser ->
                    user = loadedUser
                    if (loadedUser == null) {
                        errorMessage = resourceProvider.getString(R.string.user_not_found)
                    } else {
                        loadUserVacations(loadedUser.auth_id)
                    }
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: resourceProvider.getString(R.string.error_loading_user)
                }

            isLoading = false
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
                    vacationsError = exception.message ?: resourceProvider.getString(R.string.error_loading_vacations)
                }

            isLoadingVacations = false
        }
    }

    class Factory(
        private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
        private val getUserVacationsByIdUseCase: GetUserVacationsByIdUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileDetailsViewModel::class.java)) {
                return ProfileDetailsViewModel(
                    getUserByUsernameUseCase,
                    getUserVacationsByIdUseCase,
                    resourceProvider
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}