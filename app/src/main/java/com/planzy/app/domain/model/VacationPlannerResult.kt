package com.planzy.app.domain.model

data class VacationPlannerResult(
    val vacation: Vacation,
    val places: List<Place>
)