package com.planzy.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.planzy.app.data.repository.DeepLinkHandler
import com.planzy.app.data.util.RecoverySessionManager
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.navigation.Navigation
import com.planzy.app.ui.navigation.Register
import com.planzy.app.ui.navigation.Welcome
import com.planzy.app.ui.screens.auth.registration.DeepLinkViewModel
import com.planzy.app.ui.screens.components.BottomNavBar
import com.planzy.app.ui.theme.PlanzyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()
    private val resourceProvider by lazy { ResourceProviderImpl(this) }
    private val recoverySessionManager by lazy { RecoverySessionManager(this) }
    private val deepLinkHandler by lazy { DeepLinkHandler(resourceProvider, recoverySessionManager) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLink(intent)

        setContent {
            PlanzyTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val authRoutes = listOf(
                    Welcome.route,
                    Login.route,
                    Register.route
                )
                val showBottomBar = currentRoute !in authRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Navigation(
                        deepLinkViewModel = deepLinkViewModel,
                        navController = navController,
                        modifier = Modifier.padding(
                            bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp,
                            top = innerPadding.calculateTopPadding()
                        )
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data == null) {
            return
        }
        lifecycleScope.launch {
            val result = deepLinkHandler.handleAuthDeepLink(intent)

            deepLinkViewModel.handleDeepLinkResult(result)
        }
    }
}