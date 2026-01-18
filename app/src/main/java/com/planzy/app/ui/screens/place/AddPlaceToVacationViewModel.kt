package com.planzy.app.ui.screens.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.AddPlaceToVacationUseCase
import com.planzy.app.domain.usecase.vacation.CreateVacationUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsUseCase
import kotlinx.coroutines.launch

class AddToVacationViewModel(
    private val getUserVacationsUseCase: GetUserVacationsUseCase,
    private val createVacationUseCase: CreateVacationUseCase,
    private val addPlaceToVacationUseCase: AddPlaceToVacationUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var vacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isCreatingVacation by mutableStateOf(false)
        private set

    var isAddingPlace by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadVacations()
    }

    fun loadVacations() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            getUserVacationsUseCase()
                .onSuccess { vacations = it }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun createVacation(title: String, onSuccess: (Vacation) -> Unit) {
        viewModelScope.launch {
            isCreatingVacation = true
            errorMessage = null

            createVacationUseCase(title)
                .onSuccess { newVacation ->
                    vacations = listOf(newVacation) + vacations
                    isCreatingVacation = false
                    onSuccess(newVacation)
                }
                .onFailure { error ->
                    errorMessage = error.message
                    isCreatingVacation = false
                }
        }
    }

    fun addPlaceToVacation(vacationId: String, placeId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAddingPlace = true
            errorMessage = null
            successMessage = null

            addPlaceToVacationUseCase(vacationId, placeId)
                .onSuccess {
                    successMessage = resourceProvider.getString(R.string.place_added_to_vacation)
                    isAddingPlace = false
                    loadVacations()
                    onSuccess()
                }
                .onFailure { error ->
                    errorMessage = error.message
                    isAddingPlace = false
                }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    class Factory(
        private val getUserVacationsUseCase: GetUserVacationsUseCase,
        private val createVacationUseCase: CreateVacationUseCase,
        private val addPlaceToVacationUseCase: AddPlaceToVacationUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToVacationViewModel(
                getUserVacationsUseCase,
                createVacationUseCase,
                addPlaceToVacationUseCase,
                resourceProvider
            ) as T
        }
    }
}