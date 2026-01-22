package com.planzy.app.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.planzy.app.R
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue

@Composable
fun ProfilePictureSection(
    profilePictureUrl: String?,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ProfileImageDialog(
            profilePictureUrl = profilePictureUrl,
            onDismissRequest = { showDialog = false },
            onUploadClick = onUploadClick,
            onDeleteClick = onDeleteClick
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable(enabled = !isUploading) { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = AmaranthPurple)
            } else if (profilePictureUrl != null) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = stringResource(id = R.string.profile_picture),
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.user_profile_pic_placeholder),
                        contentDescription = stringResource(id = R.string.profile_picture),
                        modifier = Modifier.size(100.dp),
                        tint = AmericanBlue
                    )
                }
            }
        }
    }
}