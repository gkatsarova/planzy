package com.planzy.app.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.planzy.app.R
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.domain.usecase.vacation.GetFollowedUsersVacationsUseCase
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.LocationPermissionDialog
import com.planzy.app.ui.screens.components.SearchResultsOverlay
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.Raleway

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val vacationsRepository = remember { VacationsRepositoryImpl(supabaseClient = SupabaseClient, resourceProvider = resourceProvider) }
    val getFollowedUsersVacationsUseCase = remember { GetFollowedUsersVacationsUseCase(vacationsRepository) }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            getFollowedUsersVacationsUseCase = getFollowedUsersVacationsUseCase,
            resourceProvider = resourceProvider
        )
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshVacations()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(searchViewModel.locationPermissionGranted) {
        if (searchViewModel.locationPermissionGranted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        searchViewModel.setUserLocation(loc.latitude, loc.longitude)
                    } else {
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            lastLoc?.let {
                                searchViewModel.setUserLocation(it.latitude, it.longitude)
                            }
                        }
                    }
                }
                .addOnFailureListener {}
        }
    }

    LocationPermissionDialog(
        showDialog = searchViewModel.showLocationDialog,
        onDismiss = { searchViewModel.dismissLocationDialog() },
        onPermissionResult = { granted ->
            searchViewModel.setLocationPermission(granted)
        }
    )

    SearchResultsOverlay(
        searchViewModel = searchViewModel,
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = viewModel.vacationsState) {
                is VacationsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is VacationsState.Error -> {
                    Text(
                        text = state.message,
                        fontFamily = Raleway,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                is VacationsState.Success -> {
                    if (state.vacations.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.follow_users_to_see_their_vacations_here),
                            fontFamily = Raleway,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.vacations,
                                key = { vacation -> vacation.id }
                            ) { vacation ->
                                VacationCard(
                                    vacation = vacation,
                                    onCardClick = {
                                        navController.navigate(
                                            VacationDetails.createRoute(vacation.id)
                                        )
                                    },
                                    showDeleteButton = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}