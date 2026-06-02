package com.planzy.app.ui.screens.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.ui.navigation.PlaceDetails
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

    LaunchedEffect(plannerViewModel.messages.size, plannerViewModel.createdVacationId) {
        if (plannerViewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SearchResultsOverlay(
            searchViewModel = searchViewModel,
            navController = navController
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
                                modifier = Modifier.padding(top = 8.dp),
                                navController = navController
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
                }
            }
        }

        if (!isSearchActive) {
            Surface(
                color = Lavender,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .imePadding()
            ) {
                ChatInputBar(
                    onSendMessage = { plannerViewModel.sendMessage(it) }
                )
            }
        }
    }
}