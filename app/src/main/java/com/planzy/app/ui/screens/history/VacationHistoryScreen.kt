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
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.DeleteVacationDialog
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Raleway
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.screens.components.SearchResultsOverlay

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
    val userRepository = remember { UserRepositoryImpl(resourceProvider) }
    val getUserVacationsUseCase = remember { GetUserVacationsUseCase(vacationsRepository, resourceProvider) }
    val deleteVacationUseCase = remember { DeleteVacationUseCase(vacationsRepository) }
    val getUserByAuthIdUseCase = remember { GetUserByAuthIdUseCase(userRepository) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        SearchResultsOverlay(
            searchViewModel = searchViewModel,
            navController = navController
        ) {
            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
                                .padding(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Column(
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
                                                isOwner = true,
                                                onDeleteClick = {
                                                    vacationToDelete = vacation
                                                },
                                                getUserByAuthIdUseCase = getUserByAuthIdUseCase,
                                                navController = navController
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
                                                },
                                                getUserByAuthIdUseCase = getUserByAuthIdUseCase,
                                                navController = navController
                                            )
                                        }
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmaranthPurple)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}