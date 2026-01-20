package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.VacationPlace
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.VacationsRepository

class AddPlaceToVacationUseCase(
    private val vacationsRepository: VacationsRepository,
    private val placesRepository: PlacesRepository
) {

    suspend operator fun invoke(vacationId: String, placeId: String): Result<VacationPlace> {
        return try {
            val placeDetailsResult = placesRepository.getPlaceDetails(placeId)

            placeDetailsResult.fold(
                onSuccess = { place ->
                    val saveResult = placesRepository.savePlace(place)

                    saveResult.fold(
                        onSuccess = {
                            val addResult = vacationsRepository.addPlaceToVacation(vacationId, placeId)

                            addResult.fold(
                                onSuccess = { vacationPlace ->
                                    Result.success(vacationPlace)
                                },
                                onFailure = { error ->
                                    Result.failure(error)
                                }
                            )
                        },
                        onFailure = { error ->
                            Result.failure(error)
                        }
                    )
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}