package com.planzy.app.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.DateFormatter
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway

@Composable
fun VacationCard(
    vacation: Vacation,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDeleteButton: Boolean = false,
    onDeleteClick: (() -> Unit)? = null,
    getUserByAuthIdUseCase: GetUserByAuthIdUseCase? = null
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }

    LaunchedEffect(vacation.userId, getUserByAuthIdUseCase) {
        if (user == null && getUserByAuthIdUseCase != null) {
            isLoadingUser = true
            user = getUserByAuthIdUseCase(vacation.userId).getOrNull()
            isLoadingUser = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MediumBluePurple
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
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

                    Text(
                        text = user?.username ?: stringResource(id = R.string.unknown_user),
                        fontFamily = Raleway,
                        fontSize = 14.sp,
                        color = Lavender,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (showDeleteButton && onDeleteClick != null) {
                    IconButton(
                        onClick = { onDeleteClick() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_vacation),
                            tint = ErrorColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = vacation.title,
                    fontFamily = Raleway,
                    fontSize = 18.sp,
                    color = Lavender,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${vacation.placesCount} places",
                        fontFamily = Raleway,
                        fontSize = 12.sp,
                        color = Lavender
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${vacation.commentsCount} comments",
                        fontFamily = Raleway,
                        fontSize = 12.sp,
                        color = Lavender
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = DateFormatter.formatToShort(vacation.createdAt),
                        fontFamily = Raleway,
                        fontSize = 12.sp,
                        color = Lavender
                    )
                }
            }
        }
    }
}