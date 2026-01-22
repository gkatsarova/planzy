package com.planzy.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val auth_id: String,
    val username: String,
    val email: String,
    @SerialName("profile_picture_url")
    val profilePictureUrl: String? = null
)