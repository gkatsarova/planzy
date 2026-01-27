package com.planzy.app.domain.model

data class FollowStats(
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false
)