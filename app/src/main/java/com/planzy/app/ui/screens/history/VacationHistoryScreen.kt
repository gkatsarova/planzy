package com.planzy.app.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.R
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.vacation.DeleteVacationUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsUseCase
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.navigation.VacationHistory
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.DeleteVacationDialog
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun VacationHistoryScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val configuration = LocalConfiguration.current
    val sectionHeight = configuration.screenHeightDp.dp * 0.60f
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val vacationsRepository = remember { VacationsRepositoryImpl(SupabaseClient, resourceProvider) }
    val getUserVacationsUseCase = remember { GetUserVacationsUseCase(vacationsRepository, resourceProvider) }
    val deleteVacationUseCase = remember { DeleteVacationUseCase(vacationsRepository) }

    val viewModel: VacationHistoryViewModel = viewModel(
        factory = VacationHistoryViewModel.Factory(
            getUserVacationsUseCase = getUserVacationsUseCase,
            deleteVacationUseCase = deleteVacationUseCase
        )
    )

    var vacationToDelete by remember { mutableStateOf<Vacation?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.deleteErrorMessage) {
        viewModel.deleteErrorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearDeleteError()
        }
    }

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

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = VacationHistory.title,
                navController = navController,
                onSearch = { query ->
                    searchViewModel.searchForPlaces(query)
                },
                onSearchFocusChanged = { isFocused ->
                    searchViewModel.updateSearchBarFocus(isFocused)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Lavender
    ){ padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                searchViewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AmaranthPurple
                    )
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

                searchViewModel.vacations.isNotEmpty() || searchViewModel.placesWithStats.isNotEmpty() -> {
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

                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = ErrorColor,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = { viewModel.retry() }) {
                            Text(text = stringResource(id = R.string.retry))
                        }
                    }
                }

                else -> {
                    if (viewModel.myVacations.isEmpty() && viewModel.savedVacations.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.no_vacations),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Column (
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            if (viewModel.myVacations.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(sectionHeight)
                                        .padding(vertical = 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.my_vacations),
                                        fontFamily = Raleway,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmericanBlue,
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(viewModel.myVacations) { vacation ->
                                            VacationCard(
                                                vacation = vacation,
                                                onCardClick = {
                                                    navController.navigate(
                                                        VacationDetails.createRoute(vacation.id)
                                                    )
                                                },
                                                showDeleteButton = true,
                                                onDeleteClick = {
                                                    vacationToDelete = vacation
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (viewModel.savedVacations.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(sectionHeight)
                                        .padding(vertical = 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.saved_vacations),
                                        fontFamily = Raleway,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmericanBlue,
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                    )
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(viewModel.savedVacations) { vacation ->
                                            VacationCard(
                                                vacation = vacation,
                                                onCardClick = {
                                                    navController.navigate(
                                                        VacationDetails.createRoute(vacation.id)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (vacationToDelete != null) {
                DeleteVacationDialog(
                    vacation = vacationToDelete!!,
                    isDeleting = viewModel.isDeleting,
                    onConfirm = {
                        viewModel.deleteVacation(vacationToDelete!!.id)
                        vacationToDelete = null
                    },
                    onDismiss = {
                        vacationToDelete = null
                    }
                )
            }

            if (viewModel.isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmaranthPurple)
                }
            }
        }
    }
}