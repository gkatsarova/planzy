package com.planzy.app.domain.usecase.user

import com.planzy.app.data.model.User
import com.planzy.app.domain.repository.UserRepository

class GetUserByAuthIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(authId: String): Result<User?> {
        return userRepository.getUserByAuthId(authId)
    }
}