package com.planzy.app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.domain.usecase.auth.DeleteAccountUseCase
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.auth.SignOutUseCase
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.user.DeleteProfilePictureUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.domain.usecase.user.UpdateProfilePictureUseCase
import com.planzy.app.domain.usecase.user.UploadProfilePictureUseCase
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.navigation.PlaceDetails
import com.planzy.app.ui.navigation.Profile
import com.planzy.app.ui.navigation.Register
import com.planzy.app.ui.navigation.VacationDetails
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.DeleteAccountDialog
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.screens.components.ProfileCard
import com.planzy.app.ui.screens.components.ProfilePictureSection
import com.planzy.app.ui.screens.components.RetrySection
import com.planzy.app.ui.screens.components.VacationCard
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Raleway
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
){
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val authRepository = remember { AuthRepositoryImpl(resourceProvider) }
    val userRepository = remember { UserRepositoryImpl(resourceProvider) }
    val getCurrentUserUseCase = remember { GetCurrentUserUseCase(authRepository) }
    val getUserByAuthIdUseCase = remember { GetUserByAuthIdUseCase(userRepository) }
    val signOutUseCase = remember { SignOutUseCase(authRepository) }
    val deleteAccountUseCase = remember { DeleteAccountUseCase(authRepository) }
    val uploadProfilePictureUseCase = remember { UploadProfilePictureUseCase(userRepository) }
    val updateProfilePictureUseCase = remember { UpdateProfilePictureUseCase(userRepository) }
    val deleteProfilePictureUseCase = remember { DeleteProfilePictureUseCase(userRepository) }


    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(
            getCurrentUserUseCase = getCurrentUserUseCase,
            getUserByAuthIdUseCase = getUserByAuthIdUseCase,
            signOutUseCase = signOutUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            uploadProfilePictureUseCase = uploadProfilePictureUseCase,
            updateProfilePictureUseCase = updateProfilePictureUseCase,
            deleteProfilePictureUseCase = deleteProfilePictureUseCase,
            resourceProvider = resourceProvider
        )
    )

    LaunchedEffect(viewModel.isLogoutSuccessful) {
        if (viewModel.isLogoutSuccessful) {
            navController.navigate(Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(viewModel.isDeleteSuccessful) {
        if (viewModel.isDeleteSuccessful) {
            navController.navigate(Register.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                viewModel.uploadProfilePicture(file)
            } catch (_: Exception) {
                resourceProvider.getString(R.string.error_loading_profile_picture)
            }
        }
    }

    if (viewModel.showDeleteConfirmation) {
        DeleteAccountDialog(
            onConfirm = { viewModel.deleteAccount() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = Profile.title,
                navController = navController,
                onSearch = { query ->
                    searchViewModel.searchForPlaces(query)
                },
                onSearchFocusChanged = { isFocused ->
                    searchViewModel.updateSearchBarFocus(isFocused)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                searchViewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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

                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AmericanBlue
                    )
                }

                viewModel.errorMessage != null -> {
                    RetrySection(
                        errorMessage = viewModel.errorMessage!!,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfilePictureSection(
                            profilePictureUrl = viewModel.profilePictureUrl,
                            isUploading = viewModel.isUploadingPicture,
                            onUploadClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            onDeleteClick = {
                                viewModel.deleteProfilePicture()
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        ProfileCard(
                            username = viewModel.username,
                            email = viewModel.email,
                            onLogoutClick = { viewModel.signOut() },
                            onDeleteClick = { viewModel.showDeleteDialog() }
                        )
                    }
                }
            }
        }
    }
}