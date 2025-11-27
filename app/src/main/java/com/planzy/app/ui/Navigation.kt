package com.planzy.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.planzy.app.ui.screens.register.RegisterScreen
import com.planzy.app.ui.screens.login.LoginScreen
import com.planzy.app.ui.screens.welcome.WelcomeScreen

@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome_screen"){
        composable(route = "welcome_screen"){
            WelcomeScreen(navController = navController)
        }

        composable(route = "login_screen"){
            LoginScreen()
        }

        composable(route = "register_screen"){
            RegisterScreen()
        }

    }
}