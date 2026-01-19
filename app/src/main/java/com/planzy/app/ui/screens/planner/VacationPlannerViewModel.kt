package com.planzy.app.ui.screens.planner

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.ml.VacationIntentParser
import com.planzy.app.data.model.VacationPlannerResponse
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.data.repository.VacationPlannerRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.planner.CreateVacationFromTextUseCase
import com.planzy.app.ui.screens.components.ChatMessage
import kotlinx.coroutines.launch

class VacationPlannerViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createVacationFromTextUseCase: CreateVacationFromTextUseCase,
    private val recourceProvider: ResourceProviderImpl
) : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    var username by mutableStateOf<String?>(null)
        private set
    var isProcessing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var createdVacationId by mutableStateOf<String?>(null)
        private set

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            username = user?.userMetadata?.get("username")?.toString()?.removeSurrounding("\"")
                ?: recourceProvider.getString(R.string.traveller)

            if (messages.isEmpty()) {
                messages.add(ChatMessage("Hello, $username!" + recourceProvider.getString(R.string.planner_default_text1), false))
                messages.add(ChatMessage(recourceProvider.getString(R.string.planner_default_text2), false))
            }
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        messages.add(ChatMessage(userMessage, true))
        isProcessing = true
        errorMessage = null
        createdVacationId = null

        viewModelScope.launch {
            try {
                val result = createVacationFromTextUseCase(userMessage)

                result.fold(
                    onSuccess = { response ->
                        when (response) {
                            is VacationPlannerResponse.Success -> {
                                messages.add(ChatMessage(response.message, false))
                                createdVacationId = response.vacation.id
                            }
                            is VacationPlannerResponse.Error -> {
                                messages.add(ChatMessage("Sorry, ${response.message}", false))
                                errorMessage = response.message
                            }
                        }
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: recourceProvider.getString(R.string.unknown_error)
                        messages.add(ChatMessage(recourceProvider.getString(R.string.something_wrong) + errorMsg, false))
                        errorMessage = errorMsg
                    }
                )
            } catch (e: Exception) {
                messages.add(ChatMessage("Error: ${e.message}", false))
                errorMessage = e.message
            } finally {
                isProcessing = false
            }
        }
    }

    fun clearCreatedVacationId() {
        createdVacationId = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val resourceProvider = ResourceProviderImpl(context)
            val authRepository = AuthRepositoryImpl(resourceProvider, null)
            val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)

            val intentParser = VacationIntentParser(context, resourceProvider)
            val tripadvisorApi = TripadvisorApi()
            val supabaseClient = SupabaseClient
            val plannerRepository = VacationPlannerRepositoryImpl(
                intentParser,
                tripadvisorApi,
                supabaseClient,
                resourceProvider
            )
            val createVacationFromTextUseCase = CreateVacationFromTextUseCase(plannerRepository, resourceProvider)

            return VacationPlannerViewModel(
                getCurrentUserUseCase,
                createVacationFromTextUseCase,
                resourceProvider
            ) as T
        }
    }
}