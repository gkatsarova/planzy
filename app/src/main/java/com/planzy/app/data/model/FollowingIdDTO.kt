package com.planzy.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FollowingIdDTO(
    val following_id: String
) {
    val followingId: String
        get() = following_id
}