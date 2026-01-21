package com.planzy.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.DeepLinkResult
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.auth.registration.DeepLinkViewModel
import com.planzy.app.ui.screens.auth.registration.RegisterScreen
import com.planzy.app.ui.screens.auth.login.LoginScreen
import com.planzy.app.ui.screens.history.VacationHistoryScreen
import com.planzy.app.ui.screens.home.HomeScreen
import com.planzy.app.ui.screens.place.PlaceDetailsScreen
import com.planzy.app.ui.screens.planner.VacationPlannerScreen
import com.planzy.app.ui.screens.profile.ProfileScreen
import com.planzy.app.ui.screens.vacation.VacationDetailsScreen
import com.planzy.app.ui.screens.welcome.WelcomeScreen

@Composable
fun Navigation(
    deepLinkViewModel: DeepLinkViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val deepLinkResult by deepLinkViewModel.deepLinkResult.collectAsState()
    var hasHandledDeepLink by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(deepLinkResult) {
        when (deepLinkResult) {
            is DeepLinkResult.EmailVerified -> {
                if (!hasHandledDeepLink) {
                    hasHandledDeepLink = true

                    navController.navigate(Home.route) {
                        popUpTo(Welcome.route) { inclusive = true }
                    }

                    deepLinkViewModel.clearDeepLinkResult()
                    deepLinkViewModel.clearPendingCredentials()
                }
            }
            is DeepLinkResult.Error -> {
                navController.navigate(Login.route) {
                    popUpTo(Welcome.route) { inclusive = false }
                }
                deepLinkViewModel.clearDeepLinkResult()
            }
            else -> {}
        }
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { route ->
                deepLinkViewModel.saveLastRoute(route)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val searchViewModel: SearchViewModel = viewModel(
        factory = remember {
            SearchViewModel.Factory(
                context = context,
                repository = PlacesRepositoryImpl(
                    TripadvisorApi(),
                    SupabaseClient,
                    ResourceProviderImpl(context)
                ),
                entityExtractor = LocationEntityExtractor(),
                resourceProvider = ResourceProviderImpl(context),
                vacationsRepository = VacationsRepositoryImpl(
                    supabaseClient =SupabaseClient,
                    resourceProvider = ResourceProviderImpl(context)
                )
            )
        }
    )

    NavHost(navController = navController, startDestination = Welcome.route, modifier = modifier) {
        composable(route = Welcome.route) {
            WelcomeScreen(navController = navController)
        }

        composable(route = Login.route) {
            LoginScreen(
                navController = navController,
                deepLinkViewModel = deepLinkViewModel
            )
        }

        composable(route = Register.route) {
            RegisterScreen(
                navController = navController,
                deepLinkViewModel = deepLinkViewModel
            )
        }

        composable(route = Home.route) {
            HomeScreen(
                navController = navController,
                searchViewModel = searchViewModel)
        }

        composable(route = Profile.route) {
            ProfileScreen()
        }

        composable(
            route = PlaceDetails.routeWithArgs,
            arguments = listOf(
                navArgument(PlaceDetails.ARG_PLACE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString(PlaceDetails.ARG_PLACE_ID) ?: ""

            PlaceDetailsScreen(
                navController = navController,
                placeId = placeId,
                searchViewModel = searchViewModel
            )
        }

        composable(
            route = VacationDetails.routeWithArgs,
            arguments = listOf(
                navArgument(VacationDetails.ARG_VACATION_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val vacationId = backStackEntry.arguments?.getString(VacationDetails.ARG_VACATION_ID) ?: ""

            VacationDetailsScreen(
                navController = navController,
                vacationId = vacationId,
                searchViewModel = searchViewModel
            )
        }

        composable(route = VacationPlanner.route){
            VacationPlannerScreen(
                navController = navController,
                searchViewModel = searchViewModel
            )
        }

        composable(route = VacationHistory.route){
            VacationHistoryScreen(
                navController = navController,
                searchViewModel = searchViewModel
            )
        }
    }
}