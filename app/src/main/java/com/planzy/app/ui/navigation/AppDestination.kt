package com.planzy.app.ui.navigation

sealed interface AppDestination {
    val route: String
}

data object Login : AppDestination {
    override val route = "login_screen"
}

data object Register : AppDestination {
    override val route: String = "register_screen"
}

data object Welcome : AppDestination {
    override val route = "welcome_screen"
}

data object Home : AppDestination {
    override val route = "home_screen"
}

data object Profile : AppDestination {
    override val route = "profile_screen"
}