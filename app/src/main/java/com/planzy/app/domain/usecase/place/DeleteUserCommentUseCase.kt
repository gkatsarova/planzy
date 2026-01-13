package com.planzy.app.domain.usecase.place

import com.planzy.app.domain.repository.PlacesRepository

class DeleteUserCommentUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(commentId: String): Result<Unit> {
        return repository.deleteUserComment(commentId)
    }
}