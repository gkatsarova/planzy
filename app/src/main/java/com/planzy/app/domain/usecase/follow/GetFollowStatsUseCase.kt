package com.planzy.app.domain.usecase.follow

import com.planzy.app.domain.model.FollowStats
import com.planzy.app.domain.repository.FollowRepository

class GetFollowStatsUseCase(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<FollowStats> {
        return followRepository.getFollowStats(userId)
    }
}