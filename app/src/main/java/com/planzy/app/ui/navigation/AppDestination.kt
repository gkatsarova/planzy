package com.planzy.app.ui.navigation

sealed interface AppDestination {
    val route: String
    val title: String?
}

data object Login : AppDestination {
    override val route = "login_screen"
    override val title = null

}

data object Register : AppDestination {
    override val route = "register_screen"
    override val title  = null
}

data object Welcome : AppDestination {
    override val route = "welcome_screen"
    override val title = null
}

data object Home : AppDestination {
    override val route = "home_screen"
    override val title = "Home"
}

data object Profile : AppDestination {
    override val route = "profile_screen"
    override val title = "Profile"
}

data object PlaceDetails : AppDestination {
    override val route = "place_details_screen"
    override val title = "Details"

    fun createRoute(placeId: String) = "$route/$placeId"
    const val ARG_PLACE_ID = "placeId"
    val routeWithArgs = "$route/{$ARG_PLACE_ID}"
}

data object VacationDetails : AppDestination {
    override val route = "vacation_details_screen"
    override val title = "Vacation Details"

    fun createRoute(vacationId: String) = "$route/$vacationId"
    const val ARG_VACATION_ID = "vacationId"
    val routeWithArgs = "$route/{$ARG_VACATION_ID}"
}

data object VacationPlanner : AppDestination {
    override val route = "vacation_planner_screen"
    override val title = "Vacation Planner"
}

data object VacationHistory: AppDestination {
    override val route = "vacation_history_screen"
    override val title = "History"
}