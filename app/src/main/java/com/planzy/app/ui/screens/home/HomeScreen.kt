package com.planzy.app.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.navigation.Home
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.LocationPermissionDialog
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.theme.ErrorColor

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val api = remember { TripadvisorApi() }
    val repository = remember { PlacesRepositoryImpl(api) }
    val entityExtractor = remember { LocationEntityExtractor() }
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(context, repository, entityExtractor, resourceProvider)
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = Home.title,
                navController = navController,
                onSearch = { query ->
                    searchViewModel.searchForPlaces(query)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                searchViewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                searchViewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = searchViewModel.errorMessage!!,
                            color = ErrorColor,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchViewModel.places) { place ->
                            PlaceCard(
                                place = place,
                                onCardClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}