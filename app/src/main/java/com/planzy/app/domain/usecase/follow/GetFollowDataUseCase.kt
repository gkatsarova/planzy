package com.planzy.app.domain.usecase.follow

import com.planzy.app.data.model.User
import com.planzy.app.domain.model.FollowStats

class GetFollowDataUseCase(
    private val followRepository: com.planzy.app.domain.repository.FollowRepository
) {
    suspend fun getStats(userId: String): Result<FollowStats> =
        followRepository.getFollowStats(userId)

    suspend fun getFollowers(userId: String): Result<List<User>> =
        followRepository.getFollowers(userId)

    suspend fun getFollowing(userId: String): Result<List<User>> =
        followRepository.getFollowing(userId)
}