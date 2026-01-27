package com.planzy.app.ui.screens.profiledetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.planzy.app.R
import com.planzy.app.data.repository.FollowRepositoryImpl
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.data.repository.VacationsRepositoryImpl
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.follow.FollowUserUseCase
import com.planzy.app.domain.usecase.follow.GetFollowStatsUseCase
import com.planzy.app.domain.usecase.follow.GetFollowersUseCase
import com.planzy.app.domain.usecase.follow.GetFollowingUseCase
import com.planzy.app.domain.usecase.follow.UnfollowUserUseCase
import com.planzy.app.domain.usecase.user.GetUserByUsernameUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsByIdUseCase
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.components.FollowListDialog
import com.planzy.app.ui.screens.components.FollowStatsSection
import com.planzy.app.ui.screens.components.RetrySection
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.SearchResultsOverlay
import com.planzy.app.ui.theme.Raleway

@Composable
fun ProfileDetailsScreen(
    username: String,
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val userRepository = remember { UserRepositoryImpl(resourceProvider) }
    val vacationsRepository = remember { VacationsRepositoryImpl(SupabaseClient, resourceProvider) }
    val followRepository = remember { FollowRepositoryImpl(resourceProvider) }

    val getUserByUsernameUseCase = remember { GetUserByUsernameUseCase(userRepository) }
    val getUserVacationsByIdUseCase = remember { GetUserVacationsByIdUseCase(vacationsRepository) }
    val getFollowStatsUseCase = remember { GetFollowStatsUseCase(followRepository) }
    val getFollowersUseCase = remember { GetFollowersUseCase(followRepository) }
    val getFollowingUseCase = remember { GetFollowingUseCase(followRepository) }
    val followUserUseCase = remember { FollowUserUseCase(followRepository) }
    val unfollowUserUseCase = remember { UnfollowUserUseCase(followRepository) }
    val getUserByAuthIdUseCase = remember { GetUserByAuthIdUseCase(userRepository) }

    val viewModel: ProfileDetailsViewModel = viewModel(
        factory = remember {
            ProfileDetailsViewModel.Factory(
                getUserByUsernameUseCase = getUserByUsernameUseCase,
                getUserVacationsByIdUseCase = getUserVacationsByIdUseCase,
                getFollowStatsUseCase = getFollowStatsUseCase,
                getFollowersUseCase = getFollowersUseCase,
                getFollowingUseCase = getFollowingUseCase,
                followUserUseCase = followUserUseCase,
                unfollowUserUseCase = unfollowUserUseCase,
                resourceProvider = resourceProvider
            )
        }
    )

    LaunchedEffect(username) {
        viewModel.loadUserByUsername(username)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFollowStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showFollowersDialog by remember { mutableStateOf(false) }
    var showFollowingDialog by remember { mutableStateOf(false) }

    val currentUserState = viewModel.userState

    if (showFollowersDialog && currentUserState is UserState.Success) {
        FollowListDialog(
            users = viewModel.followers,
            isFollowers = true,
            isLoading = viewModel.isLoadingFollowers,
            errorMessage = viewModel.followersError,
            navController = navController,
            onDismiss = { showFollowersDialog = false }
        )
    }

    if (showFollowingDialog && currentUserState is UserState.Success) {
        FollowListDialog(
            users = viewModel.following,
            isFollowers = false,
            isLoading = viewModel.isLoadingFollowing,
            errorMessage = viewModel.followingError,
            navController = navController,
            onDismiss = { showFollowingDialog = false }
        )
    }

    SearchResultsOverlay(
        searchViewModel = searchViewModel,
        navController = navController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (val state = viewModel.userState) {
                is UserState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AmaranthPurple
                    )
                }

                is UserState.Error -> {
                    RetrySection(
                        errorMessage = state.message,
                        onRetry = { viewModel.loadUserByUsername(username) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UserState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (state.user.profilePictureUrl != null) {
                                    AsyncImage(
                                        model = state.user.profilePictureUrl,
                                        contentDescription = stringResource(id = R.string.profile_picture),
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.user_profile_pic_placeholder),
                                        contentDescription = stringResource(id = R.string.profile_picture),
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        colorFilter = ColorFilter.tint(AmericanBlue)
                                    )
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color.Transparent
                                    )
                                    .padding(vertical = 12.dp)
                            ) {
                                FollowStatsSection(
                                    followStats = viewModel.followStats,
                                    isLoading = viewModel.isLoadingFollowStats,
                                    isToggling = viewModel.isToggleFollowLoading,
                                    onFollowClick = { viewModel.toggleFollow() },
                                    onFollowersClick = {
                                        viewModel.loadFollowers(state.user.auth_id)
                                        showFollowersDialog = true
                                    },
                                    onFollowingClick = {
                                        viewModel.loadFollowing(state.user.auth_id)
                                        showFollowingDialog = true
                                    },
                                    showFollowButton = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item {
                            Text(
                                text = stringResource(id = R.string.vacations),
                                fontSize = 18.sp,
                                fontFamily = Raleway,
                                color = AmericanBlue,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }

                        when {
                            viewModel.isLoadingVacations -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = AmaranthPurple
                                        )
                                    }
                                }
                            }

                            viewModel.vacationsError != null -> {
                                item {
                                    RetrySection(
                                        errorMessage = viewModel.vacationsError!!,
                                        onRetry = {
                                            viewModel.loadUserByUsername(username)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            viewModel.vacations.isEmpty() -> {
                                item {
                                    Text(
                                        text = stringResource(id = R.string.no_vacations_user),
                                        textAlign = TextAlign.Center,
                                        fontSize = 16.sp,
                                        fontFamily = Raleway,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp)
                                    )
                                }
                            }

                            else -> {
                                items(viewModel.vacations) { vacation ->
                                    VacationCard(
                                        vacation = vacation,
                                        onCardClick = {
                                            navController.navigate(
                                                VacationDetails.createRoute(vacation.id)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        getUserByAuthIdUseCase = getUserByAuthIdUseCase
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