package com.planzy.app.ui.screens.vacation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.GetVacationDetailsUseCase
import kotlinx.coroutines.launch

class VacationDetailsViewModel(
    private val getVacationDetailsUseCase: GetVacationDetailsUseCase,
    private val vacationId: String,
    private val stringResource: ResourceProvider
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var vacation by mutableStateOf<Vacation?>(null)
        private set

    var creatorUsername by mutableStateOf<String?>(null)
        private set

    var places by mutableStateOf<List<Place>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadVacationDetails()
    }

    fun loadVacationDetails() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            getVacationDetailsUseCase(vacationId)
                .onSuccess { details ->
                    vacation = details.vacation
                    creatorUsername = details.creatorUsername
                    places = details.places
                    isLoading = false
                }
                .onFailure { exception ->
                    errorMessage = exception.message ?: stringResource.getString(R.string.unknown_error)
                    isLoading = false
                }
        }
    }

    fun onRetry() {
        loadVacationDetails()
    }

    class Factory(
        private val getVacationDetailsUseCase: GetVacationDetailsUseCase,
        private val stringResource: ResourceProvider,
        private val vacationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VacationDetailsViewModel(
                getVacationDetailsUseCase,
                vacationId,
                stringResource) as T
        }
    }
}