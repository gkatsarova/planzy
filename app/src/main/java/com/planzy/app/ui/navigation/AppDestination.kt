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