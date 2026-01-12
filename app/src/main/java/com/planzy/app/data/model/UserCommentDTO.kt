package com.planzy.app.data.model

import com.planzy.app.domain.model.UserComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCommentDTO(
    val id: String,
    @SerialName("place_id") val placeId: String,
    @SerialName("user_id") val userId: String,
    val text: String,
    val rating: Int,
    @SerialName("created_at") val createdAt: String,
    val users: UserInfo? = null
)

@Serializable
data class UserCommentInsertDTO(
    @SerialName("place_id") val placeId: String,
    @SerialName("user_id") val userId: String,
    val text: String,
    val rating: Int
)

@Serializable
data class UserInfo(
    val username: String? = null,
    val email: String? = null
)

fun UserCommentDTO.toDomainModel(currentUserId: String? = null): UserComment {
    return UserComment(
        id = id,
        placeId = placeId,
        userId = userId,
        userName = users?.username ?: "Unknown User",
        text = text,
        rating = rating,
        createdAt = createdAt,
        isOwner = userId == currentUserId
    )
}