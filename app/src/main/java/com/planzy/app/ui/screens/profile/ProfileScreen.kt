package com.planzy.app.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.planzy.app.R
import com.planzy.app.data.repository.FollowRepositoryImpl
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.domain.usecase.auth.DeleteAccountUseCase
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.auth.SignOutUseCase
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.usecase.follow.GetFollowStatsUseCase
import com.planzy.app.domain.usecase.user.DeleteProfilePictureUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.domain.usecase.user.UpdateProfilePictureUseCase
import com.planzy.app.domain.usecase.user.UploadProfilePictureUseCase
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.navigation.Register
import com.planzy.app.ui.screens.SearchViewModel
import com.planzy.app.ui.screens.components.DeleteAccountDialog
import com.planzy.app.ui.screens.components.FollowStatsSection
import com.planzy.app.ui.screens.components.GalleryPermissionDialog
import com.planzy.app.ui.screens.components.ProfileCard
import com.planzy.app.ui.screens.components.ProfilePictureSection
import com.planzy.app.ui.screens.components.RetrySection
import java.io.File
import java.io.FileOutputStream
import com.planzy.app.ui.screens.components.SearchResultsOverlay
import com.planzy.app.ui.theme.AmaranthPurple

@Composable
fun ProfileScreen(
    navController: NavController,
    searchViewModel: SearchViewModel
){
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context) }
    val authRepository = remember { AuthRepositoryImpl(resourceProvider) }
    val userRepository = remember { UserRepositoryImpl(resourceProvider) }
    val followRepository = remember { FollowRepositoryImpl(resourceProvider) }

    val getCurrentUserUseCase = remember { GetCurrentUserUseCase(authRepository) }
    val getUserByAuthIdUseCase = remember { GetUserByAuthIdUseCase(userRepository) }
    val signOutUseCase = remember { SignOutUseCase(authRepository) }
    val deleteAccountUseCase = remember { DeleteAccountUseCase(authRepository) }
    val uploadProfilePictureUseCase = remember { UploadProfilePictureUseCase(userRepository) }
    val updateProfilePictureUseCase = remember { UpdateProfilePictureUseCase(userRepository) }
    val deleteProfilePictureUseCase = remember { DeleteProfilePictureUseCase(userRepository) }
    val getFollowStatsUseCase = remember { GetFollowStatsUseCase(followRepository) }


    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(
            getCurrentUserUseCase = getCurrentUserUseCase,
            getUserByAuthIdUseCase = getUserByAuthIdUseCase,
            signOutUseCase = signOutUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            uploadProfilePictureUseCase = uploadProfilePictureUseCase,
            updateProfilePictureUseCase = updateProfilePictureUseCase,
            deleteProfilePictureUseCase = deleteProfilePictureUseCase,
            getFollowStatsUseCase = getFollowStatsUseCase,
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

    var showPermissionDialog by remember { mutableStateOf(false) }

    fun getGalleryPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    fun hasGalleryPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            getGalleryPermission()
        ) == PackageManager.PERMISSION_GRANTED
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    fun handleImagePick() {
        when {
            hasGalleryPermission() -> {
                imagePickerLauncher.launch("image/*")
            }
            else -> {
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        GalleryPermissionDialog(
            onConfirm = {
                showPermissionDialog = false
                galleryPermissionLauncher.launch(getGalleryPermission())
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }

    if (viewModel.showDeleteConfirmation) {
        DeleteAccountDialog(
            onConfirm = { viewModel.deleteAccount() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    SearchResultsOverlay(
        searchViewModel = searchViewModel,
        navController = navController
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AmaranthPurple
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
                                handleImagePick()
                            },
                            onDeleteClick = {
                                viewModel.deleteProfilePicture()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.Transparent)
                        ) {
                            FollowStatsSection(
                                followStats = viewModel.followStats,
                                isLoading = viewModel.isLoadingFollowStats,
                                isToggling = false,
                                onFollowClick = {},
                                showFollowButton = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

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