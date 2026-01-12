package com.planzy.app.domain.model

data class UserComment(
    val id: String,
    val placeId: String,
    val userId: String,
    val userName: String,
    val text: String,
    val rating: Int,
    val createdAt: String,
    val isOwner: Boolean = false
)