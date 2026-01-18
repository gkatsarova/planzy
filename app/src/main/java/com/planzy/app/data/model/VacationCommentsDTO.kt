package com.planzy.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VacationCommentDTO(
    val id: String,
    @SerialName("vacation_id") val vacationId: String,
    @SerialName("user_id") val userId: String,
    val text: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class VacationCommentInsertDTO(
    @SerialName("vacation_id") val vacationId: String,
    @SerialName("user_id") val userId: String,
    val text: String
)