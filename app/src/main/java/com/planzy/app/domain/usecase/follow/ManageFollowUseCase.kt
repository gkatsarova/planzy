package com.planzy.app.domain.usecase.follow

import com.planzy.app.domain.model.FollowStats
import com.planzy.app.domain.repository.FollowRepository

class ManageFollowUseCase(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(userId: String, currentStats: FollowStats): Result<FollowStats> {
        val result = if (currentStats.isFollowing) {
            followRepository.unfollowUser(userId)
        } else {
            followRepository.followUser(userId)
        }

        return result.map {
            currentStats.copy(
                isFollowing = !currentStats.isFollowing,
                followersCount = if (currentStats.isFollowing) {
                    currentStats.followersCount - 1
                } else {
                    currentStats.followersCount + 1
                }
            )
        }
    }
}