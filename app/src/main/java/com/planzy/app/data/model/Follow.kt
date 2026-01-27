package com.planzy.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Follow(
    val id: Long,
    val follower_id: String,
    val following_id: String,
    @SerialName("created_at")
    val createdAt: String
)