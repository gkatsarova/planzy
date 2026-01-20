package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.repository.PlacesRepository

class GetUserCommentsStatsUseCase(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(placeId: String): Result<Pair<Double?, Int>> {
        return placesRepository.getUserCommentsStats(placeId)
    }
}