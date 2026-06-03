package com.planzy.app.ui.screens.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.DeleteVacationUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsUseCase
import com.planzy.app.ui.util.toUserMessage
import kotlinx.coroutines.launch

class VacationHistoryViewModel(
    private val getUserVacationsUseCase: GetUserVacationsUseCase,
    private val deleteVacationUseCase: DeleteVacationUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var myVacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var savedVacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var deleteErrorMessage by mutableStateOf<String?>(null)
        private set

    var isDeleting by mutableStateOf(false)
        private set

    init {
        loadUserVacations()
    }

    private fun loadUserVacations() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            getUserVacationsUseCase()
                .onSuccess { (userVacations, savedVacationsList) ->

                    myVacations = userVacations
                    savedVacations = savedVacationsList

                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.toUserMessage(resourceProvider)
                }
        }
    }

    fun deleteVacation(vacationId: String) {
        viewModelScope.launch {
            isDeleting = true
            deleteErrorMessage = null

            deleteVacationUseCase(vacationId)
                .onSuccess {
                    myVacations = myVacations.filter { it.id != vacationId }
                    savedVacations = savedVacations.filter { it.id != vacationId }
                    isDeleting = false
                }
                .onFailure { error ->
                    errorMessage = error.toUserMessage(resourceProvider)
                }
        }
    }

    fun clearDeleteError() {
        deleteErrorMessage = null
    }

    fun retry() {
        loadUserVacations()
    }

    fun refreshVacations() {
        loadUserVacations()
    }

    class Factory(
        private val getUserVacationsUseCase: GetUserVacationsUseCase,
        private val deleteVacationUseCase: DeleteVacationUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VacationHistoryViewModel(
                getUserVacationsUseCase,
                deleteVacationUseCase,
                resourceProvider
            ) as T
        }
    }
}