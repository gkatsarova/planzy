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
import com.planzy.app.data.repository.*
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.planner.CreateVacationFromTextUseCase
import com.planzy.app.domain.usecase.vacation.RemovePlaceFromVacationUseCase
import com.planzy.app.ui.screens.components.ChatMessage
import kotlinx.coroutines.launch

class VacationPlannerViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createVacationFromTextUseCase: CreateVacationFromTextUseCase,
    private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase,
    private val resourceProvider: ResourceProviderImpl
) : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    var username by mutableStateOf<String?>(null)
    var isProcessing by mutableStateOf(false)
    var createdVacationId by mutableStateOf<String?>(null)
    val lastCreatedVacationPlaces = mutableStateListOf<Place>()

    init {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            username = user?.userMetadata?.get("username")?.toString()?.removeSurrounding("\"")
                ?: resourceProvider.getString(R.string.traveller)
            messages.add(ChatMessage("Hello, $username! ${resourceProvider.getString(R.string.planner_default_text1)}", false))
            messages.add(ChatMessage(resourceProvider.getString(R.string.planner_default_text2), false))
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return
        messages.add(ChatMessage(userMessage, true))
        isProcessing = true
        createdVacationId = null
        lastCreatedVacationPlaces.clear()

        viewModelScope.launch {
            createVacationFromTextUseCase(userMessage)
                .onSuccess { response ->
                    if (response is VacationPlannerResponse.Success) {
                        messages.add(ChatMessage(response.message, false))
                        messages.add(ChatMessage(response.vacation.title, false))
                        createdVacationId = response.vacation.id
                        lastCreatedVacationPlaces.addAll(response.places)
                    } else if (response is VacationPlannerResponse.Error) {
                        messages.add(ChatMessage(response.message, false))
                    }
                }
                .onFailure { error ->
                    messages.add(ChatMessage("Error: ${error.message}", false))
                }
            isProcessing = false
        }
    }

    fun removePlaceFromVacation(placeId: String) {
        val vacationId = createdVacationId ?: return
        viewModelScope.launch {
            removePlaceFromVacationUseCase(vacationId, placeId)
                .onSuccess {
                    lastCreatedVacationPlaces.removeAll { it.id == placeId }
                }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val resourceProvider = ResourceProviderImpl(context)
            val supabaseClient = SupabaseClient
            val tripadvisorApi = TripadvisorApi()
            val vacationIntentParser = VacationIntentParser(
                context,
                resourceProvider
            )
            val vacationPlannerRepository = VacationPlannerRepositoryImpl(
                vacationIntentParser,
                tripadvisorApi,
                supabaseClient,
                resourceProvider)
            val vacationRepository = VacationsRepositoryImpl(
                supabaseClient,
                resourceProvider)

            return VacationPlannerViewModel(
                GetCurrentUserUseCase(AuthRepositoryImpl(resourceProvider, null)),
                CreateVacationFromTextUseCase(vacationPlannerRepository, resourceProvider),
                RemovePlaceFromVacationUseCase(vacationRepository),
                resourceProvider
            ) as T
        }
    }
}