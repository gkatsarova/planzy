package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository

class GetUserCommentsUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(placeId: String): Result<List<UserComment>> {
        return repository.getUserComments(placeId)
    }
}