package com.planzy.app.ui.navigation

import androidx.annotation.StringRes
import com.planzy.app.R

sealed interface AppDestination {
    val route: String
    @get:StringRes
    val titleRes: Int?
}

val allDestinations = listOf(
    Login, Register, Welcome, Home, Profile,
    PlaceDetails, VacationDetails, VacationPlanner, VacationHistory, ProfileDetails
)

@StringRes
fun getTitleForRoute(currentRoute: String?): Int {
    if (currentRoute == null) return R.string.app_name

    val destination = allDestinations.find { dest ->
        currentRoute.startsWith(dest.route)
    }

    return destination?.titleRes ?: R.string.app_name
}

data object Login : AppDestination {
    override val route = "login_screen"
    override val titleRes = null

}

data object Register : AppDestination {
    override val route = "register_screen"
    override val titleRes  = null
}

data object Welcome : AppDestination {
    override val route = "welcome_screen"
    override val titleRes = null
}

data object Home : AppDestination {
    override val route = "home_screen"
    override val titleRes = R.string.title_home
}

data object Profile : AppDestination {
    override val route = "profile_screen"
    override val titleRes = R.string.title_profile
}

data object PlaceDetails : AppDestination {
    override val route = "place_details_screen"
    override val titleRes = R.string.title_details

    fun createRoute(placeId: String) = "$route/$placeId"
    const val ARG_PLACE_ID = "placeId"
    val routeWithArgs = "$route/{$ARG_PLACE_ID}"
}

data object VacationDetails : AppDestination {
    override val route = "vacation_details_screen"
    override val titleRes = R.string.title_vacation_details

    fun createRoute(vacationId: String) = "$route/$vacationId"
    const val ARG_VACATION_ID = "vacationId"
    val routeWithArgs = "$route/{$ARG_VACATION_ID}"
}

data object VacationPlanner : AppDestination {
    override val route = "vacation_planner_screen"
    override val titleRes = R.string.title_vacation_planner
}

data object VacationHistory: AppDestination {
    override val route = "vacation_history_screen"
    override val titleRes = R.string.title_history
}

data object ProfileDetails : AppDestination {
    override val route = "profile_details_screen"
    override val titleRes: Int? = null

    fun createRoute(username: String) = "$route/$username"
    const val ARG_USERNAME = "username"
    val routeWithArgs = "$route/{$ARG_USERNAME}"
}