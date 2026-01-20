package com.planzy.app.domain.usecase.place

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository

class AddUserCommentUseCase(
    private val repository: PlacesRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(placeId: String, text: String, rating: Int): Result<UserComment> {
        if (text.isBlank()) {
            return Result.failure(Exception(resourceProvider.getString(R.string.empty_comment_text)))
        }
        if (rating !in 1..5) {
            return Result.failure(Exception(resourceProvider.getString(R.string.rating_error)))
        }
        return repository.addUserComment(placeId, text, rating)
    }
}