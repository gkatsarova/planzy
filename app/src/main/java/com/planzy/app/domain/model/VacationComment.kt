package com.planzy.app.domain.model

data class VacationComment(
    val id: String,
    val vacationId: String,
    val userId: String,
    val userName: String,
    val text: String,
    val createdAt: String,
    val isOwner: Boolean = false
)