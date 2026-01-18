package com.planzy.app.ui.screens.vacation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.vacation.AddVacationCommentUseCase
import com.planzy.app.domain.usecase.vacation.DeleteVacationCommentUseCase
import com.planzy.app.domain.usecase.vacation.GetVacationCommentsUseCase
import com.planzy.app.domain.usecase.vacation.GetVacationDetailsUseCase
import com.planzy.app.domain.usecase.vacation.RemovePlaceFromVacationUseCase
import com.planzy.app.domain.usecase.vacation.UpdateVacationCommentUseCase
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.AddVacationCommentSection
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.RetrySection
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.screens.components.VacationCommentsSection
import com.planzy.app.ui.screens.components.VacationDetailsCard
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VacationDetailsScreen(
    navController: NavController,
    vacationId: String,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val tripadvisorApi = remember { TripadvisorApi() }
    val resourceProvider = remember { ResourceProviderImpl(context) }

    val placesRepository = remember { PlacesRepositoryImpl(tripadvisorApi, SupabaseClient, resourceProvider) }
    val vacationsRepository = remember { VacationsRepositoryImpl(SupabaseClient, resourceProvider) }
    val getVacationDetailsUseCase = remember { GetVacationDetailsUseCase(vacationsRepository, placesRepository) }
    val removePlaceFromVacationUseCase = remember { RemovePlaceFromVacationUseCase(vacationsRepository) }
    val getVacationCommentsUseCase = remember { GetVacationCommentsUseCase(vacationsRepository) }
    val addVacationCommentUseCase = remember { AddVacationCommentUseCase(vacationsRepository, resourceProvider) }
    val updateVacationCommentUseCase = remember { UpdateVacationCommentUseCase(vacationsRepository, resourceProvider) }
    val deleteVacationCommentUseCase = remember { DeleteVacationCommentUseCase(vacationsRepository) }

    var isEditingAnyComment by remember { mutableStateOf(false) }

    val viewModel: VacationDetailsViewModel = viewModel(
        factory = VacationDetailsViewModel.Factory(
            getVacationDetailsUseCase = getVacationDetailsUseCase,
            removePlaceFromVacationUseCase = removePlaceFromVacationUseCase,
            getVacationCommentsUseCase = getVacationCommentsUseCase,
            addVacationCommentUseCase = addVacationCommentUseCase,
            updateVacationCommentUseCase = updateVacationCommentUseCase,
            deleteVacationCommentUseCase = deleteVacationCommentUseCase,
            placesRepository = placesRepository,
            resourceProvider = resourceProvider,
            vacationId = vacationId
        )
    )

    val isSearchActive = searchViewModel.placesWithStats.isNotEmpty() ||
            searchViewModel.vacations.isNotEmpty() ||
            searchViewModel.isLoading ||
            searchViewModel.isSearchBarFocused

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = VacationDetails.title,
                navController = navController,
                onSearch = { query ->
                    searchViewModel.searchForPlaces(query)
                },
                onSearchFocusChanged = { isFocused ->
                    searchViewModel.updateSearchBarFocus(isFocused)
                }
            )
        },
        bottomBar = {
            if (!isSearchActive) {
                Surface(
                    color = Lavender,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    if (!isEditingAnyComment) {
                        AddVacationCommentSection(
                            isSubmitting = viewModel.isSubmittingComment,
                            errorMessage = viewModel.commentErrorMessage,
                            onSubmit = { text -> viewModel.addVacationComment(text) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
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
                            HorizontalDivider(
                                color = AmericanBlue,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        item {
                            Text(
                                text = stringResource(id = R.string.community_comments),
                                fontFamily = Raleway,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmericanBlue
                            )
                        }

                        item {
                            VacationCommentsSection(
                                comments = viewModel.vacationComments,
                                isLoading = viewModel.isLoadingComments,
                                errorMessage = viewModel.commentsErrorMessage,
                                onRetry = { viewModel.loadVacationComments() },
                                onEditComment = { commentId, text ->
                                    viewModel.updateVacationComment(commentId, text)
                                },
                                onDeleteComment = { commentId ->
                                    viewModel.deleteVacationComment(commentId)
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
}