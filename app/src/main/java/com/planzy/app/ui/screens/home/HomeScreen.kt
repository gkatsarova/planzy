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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.planzy.app.R
import com.planzy.app.ui.navigation.Home
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.LocationPermissionDialog
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Raleway

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
                        if (searchViewModel.vacations.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.vacations),
                                    fontFamily = Raleway,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmericanBlue,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(searchViewModel.vacations) { vacation ->
                                VacationCard(
                                    vacation = vacation,
                                    onCardClick = {
                                        searchViewModel.clearSearch()
                                        navController.navigate(VacationDetails.createRoute(vacation.id))
                                    }
                                )
                            }
                        }

                        if (searchViewModel.placesWithStats.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.places),
                                    fontFamily = Raleway,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmericanBlue,
                                    modifier = Modifier.padding(
                                        top = if (searchViewModel.vacations.isNotEmpty()) 16.dp else 8.dp,
                                        bottom = 4.dp
                                    )
                                )
                            }

                            items(searchViewModel.placesWithStats) { placeWithStats ->
                                PlaceCard(
                                    place = placeWithStats.place,
                                    onCardClick = {
                                        searchViewModel.clearSearch()
                                        navController.navigate(PlaceDetails.createRoute(placeWithStats.place.id))
                                    },
                                    userRating = placeWithStats.userRating,
                                    userReviewsCount = placeWithStats.userReviewsCount
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}