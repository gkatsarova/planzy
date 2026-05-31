package com.planzy.app.domain.usecase.place

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository

class ManagePlaceCommentsUseCase(
    private val repository: PlacesRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend fun addComment(placeId: String, text: String, rating: Int): Result<UserComment> {
        if (text.isBlank()) {
            return Result.failure(Exception(resourceProvider.getString(R.string.empty_comment_text)))
        }
        if (rating !in 1..5) {
            return Result.failure(Exception(resourceProvider.getString(R.string.rating_error)))
        }
        return repository.addUserComment(placeId, text, rating)
    }

    suspend fun updateComment(commentId: String, text: String, rating: Int): Result<Unit> {
        if (text.isBlank()) {
            return Result.failure(Exception(resourceProvider.getString(R.string.empty_comment_text)))
        }
        if (rating !in 1..5) {
            return Result.failure(Exception(resourceProvider.getString(R.string.rating_error)))
        }
        return repository.updateUserComment(commentId, text, rating)
    }

    suspend fun deleteComment(commentId: String): Result<Unit> {
        return repository.deleteUserComment(commentId)
    }
}
