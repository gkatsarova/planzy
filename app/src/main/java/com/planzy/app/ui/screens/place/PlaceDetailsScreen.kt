package com.planzy.app.ui.screens.place

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.place.*
import com.planzy.app.domain.usecase.vacation.*
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.*
import com.planzy.app.ui.theme.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import com.planzy.app.ui.navigation.VacationDetails

@OptIn(DelicateCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlaceDetailsScreen(
    navController: NavController,
    placeId: String,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val tripadvisorApi = remember { TripadvisorApi() }
    val resourceProvider = remember { ResourceProviderImpl(context) }

    val placesRepository = remember {
        PlacesRepositoryImpl(tripadvisorApi, SupabaseClient, resourceProvider)
    }

    val vacationsRepository = remember {
        VacationsRepositoryImpl(SupabaseClient, resourceProvider)
    }

    val getPlaceDetailsUseCase = remember { GetPlaceDetailsUseCase(placesRepository) }
    val getPlaceReviewsUseCase = remember { GetPlaceReviewsUseCase(placesRepository) }
    val getUserCommentsUseCase = remember { GetUserCommentsUseCase(placesRepository) }
    val addUserCommentUseCase = remember { AddUserCommentUseCase(placesRepository, resourceProvider) }
    val updateUserCommentUseCase = remember { UpdateUserCommentUseCase(placesRepository, resourceProvider) }
    val deleteUserCommentUseCase = remember { DeleteUserCommentUseCase(placesRepository) }

    val getUserVacationsUseCase = remember { GetUserVacationsUseCase(vacationsRepository) }
    val createVacationUseCase = remember { CreateVacationUseCase(vacationsRepository) }
    val addPlaceToVacationUseCase = remember { AddPlaceToVacationUseCase(vacationsRepository) }

    var isEditingAnyComment by remember { mutableStateOf(false) }
    var showAddToVacationDialog by remember { mutableStateOf(false) }

    val viewModel: PlaceDetailsViewModel = viewModel(
        factory = PlaceDetailsViewModel.Factory(
            getPlaceDetailsUseCase = getPlaceDetailsUseCase,
            getPlaceReviewsUseCase = getPlaceReviewsUseCase,
            getUserCommentsUseCase = getUserCommentsUseCase,
            addUserCommentUseCase = addUserCommentUseCase,
            updateUserCommentUseCase = updateUserCommentUseCase,
            deleteUserCommentUseCase = deleteUserCommentUseCase,
            resourceProvider = resourceProvider,
            locationId = placeId
        )
    )

    val addToVacationViewModel: AddToVacationViewModel = viewModel(
        factory = AddToVacationViewModel.Factory(
            getUserVacationsUseCase = getUserVacationsUseCase,
            createVacationUseCase = createVacationUseCase,
            addPlaceToVacationUseCase = addPlaceToVacationUseCase,
            resourceProvider = resourceProvider
        )
    )

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = PlaceDetails.title,
                navController = navController,
                onSearch = { query ->
                    searchViewModel.searchForPlaces(query)
                }
            )
        },
        bottomBar = {
            Surface(
                color = Lavender,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                if (!isEditingAnyComment) {
                    AddCommentSection(
                        isSubmitting = viewModel.isSubmittingComment,
                        errorMessage = viewModel.commentErrorMessage,
                        onSubmit = { text, rating -> viewModel.addUserComment(text, rating) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
        },
        containerColor = Lavender
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (searchViewModel.places.isNotEmpty() ||
                searchViewModel.vacations.isNotEmpty() ||
                searchViewModel.isLoading
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Lavender)) {
                    when {
                        searchViewModel.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = AmaranthPurple
                            )
                        }
                        searchViewModel.errorMessage != null -> {
                            Text(
                                text = searchViewModel.errorMessage!!,
                                color = ErrorColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center).padding(24.dp)
                            )
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
                                            modifier = Modifier.padding(top = 16.dp)
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

                                if (searchViewModel.places.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(id = R.string.places),
                                            fontFamily = Raleway,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmericanBlue,
                                            modifier = Modifier.padding(top = 16.dp)
                                        )
                                    }

                                    items(searchViewModel.places) { place ->
                                        PlaceCard(
                                            place = place,
                                            onCardClick = {
                                                searchViewModel.clearSearch()
                                                navController.navigate(
                                                    PlaceDetails.createRoute(
                                                        place.id
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
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
                        Box(modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            RetrySection(
                                errorMessage = viewModel.errorMessage!!,
                                onRetry = { viewModel.onRetry() }
                            )
                        }
                    }

                    viewModel.place != null -> {
                        val place = viewModel.place!!

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item {
                                Text(
                                    text = place.name,
                                    fontFamily = Raleway,
                                    fontSize = 32.sp,
                                    color = AmericanBlue,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }

                            item {
                                PlaceDetailsCard(
                                    place = place,
                                    onAddToVacation = { showAddToVacationDialog = true }
                                )
                            }

                            item {
                                ReviewsSection(
                                    reviews = viewModel.reviews,
                                    isLoading = viewModel.isLoadingReviews,
                                    modifier = Modifier.heightIn(max = screenHeight * 0.35f)
                                )
                            }

                            item {
                                HorizontalDivider(
                                    color = AmericanBlue,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            item {
                                Text(
                                    text = stringResource(id = R.string.community_reviews),
                                    fontFamily = Raleway,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmericanBlue
                                )
                            }

                            item {
                                UserCommentsSection(
                                    comments = viewModel.userComments,
                                    isLoading = viewModel.isLoadingUserComments,
                                    errorMessage = viewModel.userCommentsErrorMessage,
                                    onRetry = { viewModel.loadUserComments() },
                                    onEditComment = { commentId, text, rating ->
                                        viewModel.updateUserComment(commentId, text, rating)
                                    },
                                    onDeleteComment = { commentId ->
                                        viewModel.deleteUserComment(commentId)
                                    },
                                    onEditStart = { isEditingAnyComment = true },
                                    onEditCancel = { isEditingAnyComment = false },
                                    modifier = Modifier.heightIn(max = screenHeight * 0.35f)
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

        if (showAddToVacationDialog) {
            AddToVacationDialog(
                vacations = addToVacationViewModel.vacations,
                isLoading = addToVacationViewModel.isLoading,
                isCreating = addToVacationViewModel.isCreatingVacation,
                isAdding = addToVacationViewModel.isAddingPlace,
                errorMessage = addToVacationViewModel.errorMessage,
                successMessage = addToVacationViewModel.successMessage,
                onDismiss = {
                    showAddToVacationDialog = false
                    addToVacationViewModel.clearMessages()
                },
                onCreateVacation = { title ->
                    addToVacationViewModel.createVacation(title) { newVacation ->
                        addToVacationViewModel.addPlaceToVacation(newVacation.id, placeId) {
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(1500)
                                showAddToVacationDialog = false
                            }
                        }
                    }
                },
                onSelectVacation = { vacationId ->
                    addToVacationViewModel.addPlaceToVacation(vacationId, placeId) {
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(1500)
                            showAddToVacationDialog = false
                        }
                    }
                }
            )
        }
    }
}