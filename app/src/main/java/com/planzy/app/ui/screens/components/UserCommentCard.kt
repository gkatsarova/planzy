package com.planzy.app.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.DateFormatter
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.navigation.ProfileDetails
import com.planzy.app.ui.theme.*

@Composable
fun UserCommentCard(
    comment: UserComment,
    modifier: Modifier = Modifier,
    onEdit: ((commentId: String, text: String, rating: Int) -> Unit),
    onDelete: ((commentId: String) -> Unit),
    onEditStart: () -> Unit = {},
    onEditCancel: () -> Unit = {},
    navController: NavController,
    getUserByAuthIdUseCase: GetUserByAuthIdUseCase
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var user by remember { mutableStateOf<User?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }

    LaunchedEffect(comment.userId, getUserByAuthIdUseCase) {
        isLoadingUser = true
        user = getUserByAuthIdUseCase(comment.userId).getOrNull()
        isLoadingUser = false

    }

    if (isEditing) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MediumBluePurple),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit_comment),
                    fontFamily = Raleway,
                    fontSize = 12.sp,
                    color = Lavender
                )

                AddCommentSection(
                    initialText = comment.text,
                    initialRating = comment.rating,
                    isSubmitting = false,
                    onSubmit = { text, rating ->
                        onEdit.invoke(comment.id, text, rating)
                        isEditing = false
                        onEditCancel()
                    },
                    onCancel = {
                        isEditing = false
                        onEditCancel()
                    },
                    buttonText = stringResource(R.string.save)
                )
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MediumBluePurple),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (isLoadingUser) {
                        Icon(
                            painter = painterResource(id = R.drawable.user_profile_pic_placeholder),
                            contentDescription = stringResource(id = R.string.profile_picture),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            tint = Lavender
                        )
                    } else if (!user?.profilePictureUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = user?.profilePictureUrl,
                            contentDescription = stringResource(id = R.string.profile_picture),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.user_profile_pic_placeholder),
                            contentDescription = stringResource(id = R.string.profile_picture),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .padding(4.dp),
                            tint = Lavender
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.userName,
                        fontFamily = Raleway,
                        fontSize = 16.sp,
                        color = Lavender,
                        modifier = if (!comment.isOwner)
                            Modifier
                                .weight(1f)
                                .clickable {
                                comment.userName.let { username ->
                                    navController.navigate(ProfileDetails.createRoute(username))
                                }
                            }
                        else Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (index < comment.rating) Lavender else AmaranthPurple
                            )
                        }
                    }
                }

                Text(
                    text = comment.text,
                    fontFamily = Raleway,
                    fontSize = 14.sp,
                    color = Lavender,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateFormatter.formatToShort(comment.createdAt),
                        fontFamily = Raleway,
                        fontSize = 12.sp,
                        color = Lavender
                    )

                    if (comment.isOwner) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isEditing = true
                                    onEditStart()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_comment),
                                    tint = Lavender,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_comment),
                                    tint = ErrorColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteCommentDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete.invoke(comment.id)
                showDeleteDialog = false
            }
        )
    }
}