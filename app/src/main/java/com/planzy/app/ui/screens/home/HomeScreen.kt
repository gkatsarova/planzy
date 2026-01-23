package com.planzy.app.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.LocationPermissionDialog
import com.planzy.app.ui.screens.components.SearchResultsOverlay

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
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

    SearchResultsOverlay(
        searchViewModel = searchViewModel,
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}