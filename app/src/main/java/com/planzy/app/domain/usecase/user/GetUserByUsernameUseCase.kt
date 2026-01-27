package com.planzy.app.domain.usecase.user

import com.planzy.app.data.model.User
import com.planzy.app.domain.repository.UserRepository

class GetUserByUsernameUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Result<User?> {
        return userRepository.getUserByUsername(username)
    }
}