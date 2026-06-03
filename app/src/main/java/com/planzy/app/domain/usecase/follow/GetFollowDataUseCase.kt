package com.planzy.app.domain.usecase.follow

import com.planzy.app.domain.model.FollowDomainModel
import com.planzy.app.domain.repository.FollowRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetFollowDataUseCase(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(userId: String): Result<FollowDomainModel> = coroutineScope {
        val statsDeferred = async { followRepository.getFollowStats(userId) }
        val followersDeferred = async { followRepository.getFollowers(userId) }
        val followingDeferred = async { followRepository.getFollowing(userId) }

        try {
            val stats = statsDeferred.await().getOrThrow()
            val followers = followersDeferred.await().getOrThrow()
            val following = followingDeferred.await().getOrThrow()

            Result.success(
                FollowDomainModel(
                    followersCount = stats.followersCount,
                    followingCount = stats.followingCount,
                    isFollowing = stats.isFollowing,
                    followers = followers,
                    following = following
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}