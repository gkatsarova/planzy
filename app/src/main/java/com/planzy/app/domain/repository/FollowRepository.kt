package com.planzy.app.domain.repository

import com.planzy.app.data.model.User
import com.planzy.app.domain.model.FollowStats

interface FollowRepository {
    suspend fun followUser(followingId: String): Result<Unit>

    suspend fun unfollowUser(followingId: String): Result<Unit>

    suspend fun getFollowStats(userId: String): Result<FollowStats>

    suspend fun isFollowing(userId: String): Result<Boolean>

    suspend fun getFollowers(userId: String): Result<List<User>>

    suspend fun getFollowing(userId: String): Result<List<User>>
}