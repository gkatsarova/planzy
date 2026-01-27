package com.planzy.app.domain.usecase.follow

import com.planzy.app.data.model.User
import com.planzy.app.domain.repository.FollowRepository

class GetFollowingUseCase(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<List<User>> {
        return followRepository.getFollowing(userId)
    }
}