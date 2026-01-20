package com.planzy.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedVacationDTO(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("vacation_id")
    val vacationId: String,
    @SerialName("saved_at")
    val savedAt: String
)