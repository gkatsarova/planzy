package com.planzy.app.domain.usecase.follow

import com.planzy.app.domain.repository.FollowRepository

class UnfollowUserUseCase(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return followRepository.unfollowUser(userId)
    }
}