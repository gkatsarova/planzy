package com.planzy.app.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.planzy.app.domain.model.Place
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.R
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway
import java.util.Locale

@Composable
fun PlaceCard(
    place: Place,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    userRating: Double? = null,
    userReviewsCount: Int? = null,
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MediumBluePurple
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MediumBluePurple),
                    contentAlignment = Alignment.Center
                ) {
                    if (place.photoUrl != null) {
                        AsyncImage(
                            model = place.photoUrl,
                            contentDescription = place.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = {
                                android.util.Log.e("PlaceCard", "Failed to load image for ${place.name}: ${place.photoUrl}")
                            },
                            onSuccess = {
                                android.util.Log.d("PlaceCard", "Image loaded for ${place.name}")
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = stringResource(id = R.string.no_image),
                            modifier = Modifier.size(50.dp),
                            tint = Lavender
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (showRemoveButton) 36.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = place.name,
                        fontFamily = Raleway,
                        fontSize = 18.sp,
                        maxLines = 2,
                        color = Lavender
                    )

                    place.category?.let { category ->
                        Text(
                            text = category,
                            fontFamily = Raleway,
                            fontSize = 13.sp,
                            color = Lavender.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = place.location.address,
                            fontFamily = Raleway,
                            fontSize = 13.sp,
                            maxLines = 1,
                            color = Lavender.copy(alpha = 0.9f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tripadvisor),
                            contentDescription = stringResource(id = R.string.tripadvisor),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = place.rating.toString(),
                                fontFamily = Raleway,
                                fontSize = 13.sp,
                                color = Lavender
                            )
                        }

                        Text(
                            text = "(${place.reviewsCount})",
                            fontFamily = Raleway,
                            fontSize = 12.sp,
                            color = Lavender.copy(alpha = 0.8f)
                        )
                    }

                    if (userRating != null && userReviewsCount != null && userReviewsCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", userRating),
                                    fontFamily = Raleway,
                                    fontSize = 13.sp,
                                    color = Lavender
                                )
                            }

                            Text(
                                text = "($userReviewsCount ${stringResource(id = R.string.user_reviews)})",
                                fontFamily = Raleway,
                                fontSize = 12.sp,
                                color = Lavender.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (showRemoveButton) {
                IconButton(
                    onClick = { onRemoveClick() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_remove),
                        contentDescription = stringResource(id = R.string.remove_from_vacation),
                        tint = Lavender,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}