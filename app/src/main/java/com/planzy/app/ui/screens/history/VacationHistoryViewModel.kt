package com.planzy.app.ui.screens.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.GetUserVacationsUseCase
import kotlinx.coroutines.launch

class VacationHistoryViewModel(
    private val getUserVacationsUseCase: GetUserVacationsUseCase
) : ViewModel() {

    var vacations by mutableStateOf<List<Vacation>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadUserVacations()
    }

    private fun loadUserVacations() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            getUserVacationsUseCase()
                .onSuccess {
                    vacations = it
                    isLoading = false
                }
                .onFailure {
                    errorMessage = it.message
                    isLoading = false
                }
        }
    }

    fun retry() {
        loadUserVacations()
    }

    class Factory(
        private val getUserVacationsUseCase: GetUserVacationsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VacationHistoryViewModel(getUserVacationsUseCase) as T
        }
    }
}