package com.planzy.app.domain.usecase.user

import com.planzy.app.data.model.User
import com.planzy.app.domain.repository.UserRepository

class SearchUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        return userRepository.searchUsers(query)
    }
}