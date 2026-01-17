package com.planzy.app.ui.screens.vacation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.vacation.GetVacationDetailsUseCase
import com.planzy.app.domain.usecase.vacation.RemovePlaceFromVacationUseCase
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.RetrySection
import com.planzy.app.ui.screens.components.VacationDetailsCard
import com.planzy.app.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VacationDetailsScreen(
    navController: NavController,
    vacationId: String
) {
    val context = LocalContext.current

    val tripadvisorApi = remember { TripadvisorApi() }
    val resourceProvider = remember { ResourceProviderImpl(context) }

    val placesRepository = remember {
        PlacesRepositoryImpl(tripadvisorApi, SupabaseClient, resourceProvider)
    }

    val vacationsRepository = remember {
        VacationsRepositoryImpl(SupabaseClient, resourceProvider)
    }

    val getVacationDetailsUseCase = remember {
        GetVacationDetailsUseCase(vacationsRepository, placesRepository)
    }

    val removePlaceFromVacationUseCase = remember {
        RemovePlaceFromVacationUseCase(vacationsRepository)
    }

    val viewModel: VacationDetailsViewModel = viewModel(
        factory = VacationDetailsViewModel.Factory(
            getVacationDetailsUseCase = getVacationDetailsUseCase,
            removePlaceFromVacationUseCase = removePlaceFromVacationUseCase,
            placesRepository = placesRepository,
            recourceProvider = resourceProvider,
            vacationId = vacationId
        )
    )

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = VacationDetails.title,
                navController = navController,
                onSearch = { }
            )
        },
        containerColor = Lavender
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AmaranthPurple)
                    }
                }

                viewModel.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        RetrySection(
                            errorMessage = viewModel.errorMessage!!,
                            onRetry = { viewModel.onRetry() }
                        )
                    }
                }

                viewModel.vacation != null && viewModel.creatorUsername != null -> {
                    val vacation = viewModel.vacation!!
                    val creatorUsername = viewModel.creatorUsername!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            Text(
                                text = vacation.title,
                                fontFamily = Raleway,
                                fontSize = 32.sp,
                                color = AmericanBlue
                            )
                        }

                        item {
                            VacationDetailsCard(
                                places = viewModel.places,
                                creatorUsername = creatorUsername,
                                createdAt = vacation.createdAt,
                                onPlaceClick = { place ->
                                    navController.navigate(PlaceDetails.createRoute(place.id))
                                },
                                onRemovePlace = { place ->
                                    viewModel.removePlaceFromVacation(place.id)
                                },
                                isOwner = viewModel.isOwner,
                                getUserRating = { placeId ->
                                    viewModel.getUserRating(placeId)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}