package com.planzy.app.ui.screens.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.screens.components.PlaceDetailsCard
import com.planzy.app.ui.theme.*
import com.planzy.app.R
import com.planzy.app.ui.screens.components.RetrySection

@Composable
fun PlaceDetailsScreen(
    navController: NavController,
    placeId: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val tripadvisorApi = remember { TripadvisorApi() }
    val repository = remember { PlacesRepositoryImpl(tripadvisorApi) }
    val resourceProvider = remember { ResourceProviderImpl(context) }

    val viewModel: PlaceDetailsViewModel = viewModel(
        factory = PlaceDetailsViewModel.Factory(
            repository = repository,
            resourceProvider = resourceProvider,
            locationId = placeId
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Lavender
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MediumBluePurple
                    )
                }

                viewModel.errorMessage != null -> {
                    RetrySection(
                        errorMessage = viewModel.errorMessage!!,
                        onRetry = { viewModel.onRetry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                viewModel.place != null -> {
                    val place = viewModel.place!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(70.dp))

                        Text(
                            text = place.name,
                            fontFamily = Raleway,
                            fontSize = 32.sp,
                            color = AmericanBlue
                        )

                        PlaceDetailsCard(place = place)

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(MediumBluePurple, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = Lavender
                )
            }
        }
    }
}