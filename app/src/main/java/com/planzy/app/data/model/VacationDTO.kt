package com.planzy.app.data.model

import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationPlace
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VacationDTO(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class VacationInsertDTO(
    @SerialName("user_id")
    val userId: String,
    val title: String
)

@Serializable
data class VacationPlaceDTO(
    val id: String,
    @SerialName("vacation_id")
    val vacationId: String,
    @SerialName("place_id")
    val placeId: String,
    @SerialName("order_index")
    val orderIndex: Int,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class VacationPlaceInsertDTO(
    @SerialName("vacation_id")
    val vacationId: String,
    @SerialName("place_id")
    val placeId: String,
    @SerialName("order_index")
    val orderIndex: Int
)

@Serializable
data class OrderIndexDTO(
    @SerialName("order_index")
    val orderIndex: Int
)

@Serializable
data class VacationIdDTO(
    val id: String
)

@Serializable
data class VacationPlaceSimpleDTO(
    @SerialName("place_id")
    val placeId: String,
    @SerialName("order_index")
    val orderIndex: Int
)

fun VacationDTO.toDomainModel() = Vacation(
    id = id,
    userId = userId,
    title = title,
    createdAt = createdAt,
    placesCount = 0
)

fun VacationPlaceDTO.toDomainModel() = VacationPlace(
    id = id,
    vacationId = vacationId,
    placeId = placeId,
    orderIndex = orderIndex,
    createdAt = createdAt
)