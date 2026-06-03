package com.planzy.app.domain.model

import com.planzy.app.data.model.User

data class FollowDomainModel(
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean,
    val followers: List<User>,
    val following: List<User>
)