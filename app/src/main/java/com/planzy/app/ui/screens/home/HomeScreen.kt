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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.ui.screens.components.LocationPermissionDialog
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val api = remember { TripadvisorApi() }
    val repository = remember { PlacesRepositoryImpl(api) }
    val entityExtractor = remember { LocationEntityExtractor() }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(context, repository, entityExtractor)
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(viewModel.locationPermissionGranted) {
        if (viewModel.locationPermissionGranted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    loc?.let { viewModel.setUserLocation(it.latitude, it.longitude) }
                }
        }
    }

    LocationPermissionDialog(
        showDialog = viewModel.showLocationDialog,
        onDismiss = { viewModel.dismissLocationDialog() },
        onPermissionResult = { granted ->
            viewModel.setLocationPermission(granted)
        }
    )

    Scaffold(
        topBar = {
            PlanzyTopAppBar(title = "Home", navController)
            { viewModel.searchForPlaces(it) }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.places) { place ->
                        PlaceCard(place = place, onCardClick = { })
                    }
                }
            }
        }
    }
}