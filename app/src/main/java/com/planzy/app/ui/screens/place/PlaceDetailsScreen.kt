package com.planzy.app.ui.screens.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.place.AddUserCommentUseCase
import com.planzy.app.domain.usecase.place.GetPlaceDetailsUseCase
import com.planzy.app.domain.usecase.place.GetPlaceReviewsUseCase
import com.planzy.app.domain.usecase.place.GetUserCommentsUseCase
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.AddCommentSection
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlaceDetailsCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.RetrySection
import com.planzy.app.ui.screens.components.ReviewsSection
import com.planzy.app.ui.screens.components.UserCommentsSection
import com.planzy.app.ui.theme.*

@Composable
fun PlaceDetailsScreen(
    navController: NavController,
    placeId: String,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val tripadvisorApi = remember { TripadvisorApi() }
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val repository = remember {
        PlacesRepositoryImpl(tripadvisorApi, SupabaseClient, resourceProvider)
    }

    val getPlaceDetailsUseCase = remember { GetPlaceDetailsUseCase(repository) }
    val getPlaceReviewsUseCase = remember { GetPlaceReviewsUseCase(repository) }
    val getUserCommentsUseCase = remember { GetUserCommentsUseCase(repository) }
    val addUserCommentUseCase = remember { AddUserCommentUseCase(repository, resourceProvider) }

    val viewModel: PlaceDetailsViewModel = viewModel(
        factory = PlaceDetailsViewModel.Factory(
            getPlaceDetailsUseCase = getPlaceDetailsUseCase,
            getPlaceReviewsUseCase = getPlaceReviewsUseCase,
            getUserCommentsUseCase = getUserCommentsUseCase,
            addUserCommentUseCase = addUserCommentUseCase,
            resourceProvider = resourceProvider,
            locationId = placeId
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
        containerColor = Lavender
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (searchViewModel.places.isNotEmpty() || searchViewModel.isLoading) {

                Box(modifier = Modifier.fillMaxSize().background(Lavender)) {
                    when {
                        searchViewModel.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MediumBluePurple
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
                                items(searchViewModel.places) { place ->
                                    PlaceCard(
                                        place = place,
                                        onCardClick = {
                                            searchViewModel.clearSearch()
                                            navController.navigate(PlaceDetails.createRoute(place.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
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
                            Text(
                                text = place.name,
                                fontFamily = Raleway,
                                fontSize = 32.sp,
                                color = AmericanBlue,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            PlaceDetailsCard(place = place)

                            ReviewsSection(
                                reviews = viewModel.reviews,
                                isLoading = viewModel.isLoadingReviews
                            )

                            HorizontalDivider(color = AmericanBlue.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))

                            Text(
                                text = stringResource(id = R.string.community_reviews),
                                fontFamily = Raleway,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmericanBlue
                            )

                            UserCommentsSection(
                                comments = viewModel.userComments,
                                isLoading = viewModel.isLoadingUserComments,
                                errorMessage = viewModel.userCommentsErrorMessage,
                                onRetry = { viewModel.loadUserComments() }
                            )

                            AddCommentSection(
                                isSubmitting = viewModel.isSubmittingComment,
                                errorMessage = viewModel.commentErrorMessage,
                                onSubmit = { text, rating ->
                                    viewModel.addUserComment(text, rating)
                                }
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}