package com.planzy.app.ui.screens.planner

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.navigation.VacationPlanner
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.ChatBubble
import com.planzy.app.ui.screens.components.ChatInputBar
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
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

    LaunchedEffect(plannerViewModel.messages.size) {
        if (plannerViewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(plannerViewModel.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = VacationPlanner.title,
                navController = navController,
                onSearch = { query ->
                    searchViewModel.searchForPlaces(query)
                },
                onSearchFocusChanged = { isFocused ->
                    searchViewModel.updateSearchBarFocus(isFocused)
                }
            )
        },
        containerColor = Lavender
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!isSearchActive) {
                                    items(plannerViewModel.messages) { message ->
                                        ChatBubble(message = message)
                                    }
                                }

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

            if (!isSearchActive) {
                ChatInputBar(onSendMessage = { })
            }
        }
    }
}