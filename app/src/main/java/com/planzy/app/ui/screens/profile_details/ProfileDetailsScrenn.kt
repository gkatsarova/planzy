package com.planzy.app.ui.screens.profile_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.planzy.app.R
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import androidx.compose.ui.graphics.ColorFilter

@Composable
fun ProfileDetailsScreen(
    username: String
) {
    val context = LocalContext.current

    val viewModel: ProfileDetailsViewModel = viewModel(
        factory = remember {
            ProfileDetailsViewModel.Factory(
                userRepository = UserRepositoryImpl(ResourceProviderImpl(context)),
                resourceProvider = ResourceProviderImpl(context)
            )
        }
    )

    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(username) {
        viewModel.loadUserByUsername(username)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AmaranthPurple
                )
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = ErrorColor,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            user != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (user!!.profilePictureUrl != null) {
                        AsyncImage(
                            model = user!!.profilePictureUrl,
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
        }
    }
}