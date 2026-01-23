package com.planzy.app.ui.screens.profile_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileDetailsViewModel(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadUserByUsername(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            userRepository.getUserByUsername(username)
                .onSuccess { user ->
                    _user.value = user
                    if (user == null) {
                        _errorMessage.value = resourceProvider.getString(R.string.user_not_found)
                    }
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: resourceProvider.getString(R.string.error_loading_user)
                }

            _isLoading.value = false
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileDetailsViewModel::class.java)) {
                return ProfileDetailsViewModel(
                    userRepository,
                    resourceProvider
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}