package com.planzy.app.domain.usecase.vacation

import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.VacationsRepository

data class VacationDetails(
    val vacation: Vacation,
    val creatorUsername: String,
    val places: List<Place>
)

class GetVacationDetailsUseCase(
    private val vacationsRepository: VacationsRepository,
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(vacationId: String): Result<VacationDetails> {
        return try {
            val vacationResult = vacationsRepository.getVacationWithUser(vacationId)

            vacationResult.fold(
                onSuccess = { (vacation, username) ->
                    val placeIdsResult = vacationsRepository.getVacationPlaceIds(vacationId)

                    placeIdsResult.fold(
                        onSuccess = { placeIds ->
                             val places = placeIds.mapIndexedNotNull { index, placeId ->
                                val placeResult = placesRepository.getPlaceDetails(placeId)
                                placeResult.fold(
                                    onSuccess = { place ->
                                        place
                                    },
                                    onFailure = { error ->
                                        null
                                    }
                                )
                            }

                            Result.success(
                                VacationDetails(
                                    vacation = vacation,
                                    creatorUsername = username,
                                    places = places
                                )
                            )
                        },
                        onFailure = { exception ->
                            Result.failure(exception)
                        }
                    )
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}