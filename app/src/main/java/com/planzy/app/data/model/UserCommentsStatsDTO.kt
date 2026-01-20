package com.planzy.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserCommentStatsDTO(
    val rating: Int
)