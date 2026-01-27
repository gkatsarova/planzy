package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.planzy.app.R
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.ProfileDetails
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway


@Composable
fun SearchResultsOverlay(
    searchViewModel: SearchViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val userRepository = remember { UserRepositoryImpl(resourceProvider) }
    val getUserByAuthIdUseCase = remember { GetUserByAuthIdUseCase(userRepository) }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        val hasResults = searchViewModel.vacations.isNotEmpty() || searchViewModel.placesWithStats.isNotEmpty() || searchViewModel.users.isNotEmpty()
        val isSearching = searchViewModel.searchQuery.isNotEmpty() &&
                (searchViewModel.isLoading || searchViewModel.errorMessage != null || hasResults)

        if (isSearching) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Lavender
            ) {
                when {
                    searchViewModel.isLoading -> {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = AmaranthPurple
                            )
                        }
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
                    hasResults -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (searchViewModel.users.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(id = R.string.profiles),
                                        fontFamily = Raleway,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmericanBlue,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                items(searchViewModel.users) { user ->
                                    UserCard(
                                        user = user,
                                        onCardClick = {
                                            searchViewModel.clearSearch()
                                            navController.navigate(ProfileDetails.createRoute(user.username))
                                        }
                                    )
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
                                        },
                                        getUserByAuthIdUseCase = getUserByAuthIdUseCase
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
    }
}