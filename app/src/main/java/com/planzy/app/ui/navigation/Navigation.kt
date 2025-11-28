package com.planzy.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.planzy.app.ui.screens.registration.RegisterScreen
import com.planzy.app.ui.screens.login.LoginScreen
import com.planzy.app.ui.screens.welcome.WelcomeScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Welcome.route) {
        composable(route = Welcome.route) {
            WelcomeScreen(navController = navController)
        }

        composable(route = Login.route) {
            LoginScreen()
        }

        composable(route = Register.route) {
            RegisterScreen()
        }

    }
}