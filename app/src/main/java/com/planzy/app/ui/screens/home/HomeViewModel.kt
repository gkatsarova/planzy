package com.planzy.app.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.GetFollowedUsersVacationsUseCase
import kotlinx.coroutines.launch

sealed interface VacationsState {
    data object Loading : VacationsState
    data class Success(val vacations: List<Vacation>) : VacationsState
    data class Error(val message: String) : VacationsState
}

class HomeViewModel(
    private val getFollowedUsersVacationsUseCase: GetFollowedUsersVacationsUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var vacationsState by mutableStateOf<VacationsState>(VacationsState.Loading)
        private set

    init {
        loadFollowedUsersVacations()
    }

    fun loadFollowedUsersVacations() {
        viewModelScope.launch {
            vacationsState = VacationsState.Loading

            getFollowedUsersVacationsUseCase()
                .onSuccess { vacations ->
                    vacationsState = VacationsState.Success(vacations)
                }
                .onFailure { exception ->
                    vacationsState = VacationsState.Error(
                        exception.message ?: resourceProvider.getString(R.string.error_loading_vacations)
                    )
                }
        }
    }

    class Factory(
        private val getFollowedUsersVacationsUseCase: GetFollowedUsersVacationsUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(
                    getFollowedUsersVacationsUseCase,
                    resourceProvider
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}