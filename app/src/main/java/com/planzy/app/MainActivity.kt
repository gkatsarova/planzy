package com.planzy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.data.repository.DeepLinkHandler
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.RecoverySessionManager
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.navigation.Navigation
import com.planzy.app.ui.navigation.ProfileDetails
import com.planzy.app.ui.navigation.Register
import com.planzy.app.ui.navigation.Welcome
import com.planzy.app.ui.navigation.getTitleForRoute
import com.planzy.app.ui.screens.PlanzyTopBarViewModel
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.auth.registration.DeepLinkViewModel
import com.planzy.app.ui.screens.components.BottomNavBar
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.theme.PlanzyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()
    private val resourceProvider by lazy { ResourceProviderImpl(this) }
    private val recoverySessionManager by lazy { RecoverySessionManager(this) }
    private val deepLinkHandler by lazy { DeepLinkHandler(resourceProvider, recoverySessionManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SupabaseClient.initialize()

        handleDeepLink(intent)

        setContent {
            PlanzyTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val authRepository = remember { AuthRepositoryImpl(resourceProvider) }
                val userRepository = remember { UserRepositoryImpl(resourceProvider) }
                val getCurrentUserUseCase = remember { GetCurrentUserUseCase(authRepository) }
                val getUserByAuthIdUseCase = remember { GetUserByAuthIdUseCase(userRepository) }


                val authRoutes = listOf(
                    Welcome.route,
                    Login.route,
                    Register.route
                )
                val showBars = currentRoute !in authRoutes

                val plazyTopBarViewModel: PlanzyTopBarViewModel = viewModel(
                    factory = PlanzyTopBarViewModel.Factory(
                        getCurrentUserUseCase = getCurrentUserUseCase,
                        getUserByAuthIdUseCase = getUserByAuthIdUseCase
                    )
                )

                val searchViewModel: SearchViewModel = viewModel(
                    factory = remember {
                        SearchViewModel.Factory(
                            context = this@MainActivity,
                            repository = PlacesRepositoryImpl(
                                TripadvisorApi(),
                                SupabaseClient,
                                ResourceProviderImpl(this@MainActivity)
                            ),
                            entityExtractor = LocationEntityExtractor(),
                            resourceProvider = ResourceProviderImpl(this@MainActivity),
                            vacationsRepository = VacationsRepositoryImpl(
                                supabaseClient =SupabaseClient,
                                resourceProvider = ResourceProviderImpl(this@MainActivity)
                            ),
                            userRepository = UserRepositoryImpl(ResourceProviderImpl(this@MainActivity))
                        )
                    }
                )

                val title = when {
                    currentRoute?.startsWith(ProfileDetails.route) == true -> {
                        navBackStackEntry?.arguments?.getString(ProfileDetails.ARG_USERNAME) ?: "Profile"
                    }
                    else -> getTitleForRoute(currentRoute)
                }

                Scaffold(
                    topBar = {
                        if (showBars) {
                            PlanzyTopAppBar(
                                title = title,
                                profilePictureUrl = plazyTopBarViewModel.profilePictureUrl,
                                navController = navController,
                                searchQuery = searchViewModel.searchQuery,
                                onSearch = { query -> searchViewModel.search(query) }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBars) {
                            BottomNavBar(navController = navController)
                        }
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Navigation(
                        deepLinkViewModel = deepLinkViewModel,
                        navController = navController,
                        searchViewModel = searchViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
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