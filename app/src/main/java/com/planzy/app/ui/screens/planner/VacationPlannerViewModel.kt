package com.planzy.app.ui.screens.planner

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.ui.screens.components.ChatMessage
import kotlinx.coroutines.launch

class VacationPlannerViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    var username by mutableStateOf<String?>(null)
        private set

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            username = user?.userMetadata?.get("username")?.toString()?.removeSurrounding("\"")
                        ?: "Traveler"

            if (messages.isEmpty()) {
                messages.add(ChatMessage("Hello, $username! What type of vacation are you looking for?", false))
                messages.add(ChatMessage("Describe your dream vacation (e.g. 5 days in Rome with art and history)...", false))
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val resourceProvider = ResourceProviderImpl(context)
            val authRepository = AuthRepositoryImpl(resourceProvider, null)
            val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)

            return VacationPlannerViewModel(getCurrentUserUseCase) as T
        }
    }
}