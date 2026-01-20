package com.planzy.app.ui.screens.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.R
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.navigation.VacationPlanner
import com.planzy.app.ui.screens.components.*
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.theme.*

@Composable
fun VacationPlannerScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val plannerViewModel: VacationPlannerViewModel = viewModel(
        factory = VacationPlannerViewModel.Factory(context)
    )

    val isSearchActive = searchViewModel.placesWithStats.isNotEmpty() ||
            searchViewModel.vacations.isNotEmpty() ||
            searchViewModel.isLoading ||
            searchViewModel.isSearchBarFocused

    val listState = rememberLazyListState()

    var isChatFocused by remember { mutableStateOf(false) }

    LaunchedEffect(plannerViewModel.messages.size, plannerViewModel.createdVacationId) {
        if (plannerViewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
        }
    }

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = VacationPlanner.title,
                navController = navController,
                onSearch = { searchViewModel.searchForPlaces(it) },
                onSearchFocusChanged = { searchViewModel.updateSearchBarFocus(it) }
            )
        },
        bottomBar = {
            if (!isSearchActive) {
                Surface(
                    color = Lavender,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    shadowElevation = 8.dp
                ) {
                    ChatInputBar(
                        onSendMessage = { plannerViewModel.sendMessage(it) },
                        onFocusChange = { isChatFocused = it }
                    )
                }
            }
        },
        containerColor = Lavender,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.padding(bottom = if (!isChatFocused && !isSearchActive) 80.dp else 0.dp)

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isSearchActive) {
                    items(plannerViewModel.messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (plannerViewModel.createdVacationId != null) {
                        item {
                            VacationDetailsCard(
                                places = plannerViewModel.lastCreatedVacationPlaces,
                                creatorUsername = "",
                                createdAt = "",
                                onPlaceClick = { place ->
                                    navController.navigate(PlaceDetails.createRoute(place.id))
                                },
                                onRemovePlace = { place ->
                                    plannerViewModel.removePlaceFromVacation(place.id)
                                },
                                isOwner = true,
                                getUserRating = { Pair(null, 0) },
                                showMetadata = false,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    if (plannerViewModel.isProcessing) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AmaranthPurple)
                            }
                        }
                    }
                } else {
                    if (searchViewModel.isLoading) {
                        item {
                            Box(
                                Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AmaranthPurple)
                            }
                        }
                    } else {
                        if (searchViewModel.vacations.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.vacations),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(searchViewModel.vacations) { v ->
                                VacationCard(
                                    v,
                                    onCardClick = {
                                        searchViewModel.clearSearch()
                                        navController.navigate(
                                            VacationDetails.createRoute(v.id)
                                        )
                                    }
                                )
                            }
                        }
                        if (searchViewModel.placesWithStats.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.places),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(searchViewModel.placesWithStats) { p ->
                                PlaceCard(
                                    place = p.place,
                                    onCardClick = {
                                        searchViewModel.clearSearch()
                                        navController.navigate(PlaceDetails.createRoute(p.place.id))
                                    },
                                    userRating = p.userRating,
                                    userReviewsCount = p.userReviewsCount
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}