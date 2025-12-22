package com.planzy.app.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.planzy.app.data.repository.DeepLinkResult
import com.planzy.app.ui.screens.auth.registration.DeepLinkViewModel
import com.planzy.app.ui.screens.auth.registration.RegisterScreen
import com.planzy.app.ui.screens.auth.login.LoginScreen
import com.planzy.app.ui.screens.welcome.WelcomeScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val deepLinkViewModel: DeepLinkViewModel = viewModel()
    val context = LocalContext.current

    val deepLinkResult by deepLinkViewModel.deepLinkResult.collectAsState()

    LaunchedEffect(deepLinkResult) {
        when (val result = deepLinkResult) {
            is DeepLinkResult.EmailVerified -> {
                Toast.makeText(
                    context,
                    "Email is verified",
                    Toast.LENGTH_LONG
                ).show()

                deepLinkViewModel.clearDeepLinkResult()
            }
            is DeepLinkResult.Error -> {
                println("Error in navigation: ${result.message}")

                Toast.makeText(
                    context,
                    "Error: ${result.message}",
                    Toast.LENGTH_LONG
                ).show()

                deepLinkViewModel.clearDeepLinkResult()
            }
            is DeepLinkResult.PasswordRecovery -> {
                deepLinkViewModel.clearDeepLinkResult()
            }
            DeepLinkResult.NoDeepLink, DeepLinkResult.Unknown -> {

            }
        }
    }

    NavHost(navController = navController, startDestination = Welcome.route) {
        composable(route = Welcome.route) {
            WelcomeScreen(navController = navController)
        }

        composable(route = Login.route) {
            LoginScreen()
        }

        composable(route = Register.route) {
            RegisterScreen(navController = navController)
        }
    }
}