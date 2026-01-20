package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.R
import com.planzy.app.data.util.DateFormatter
import com.planzy.app.domain.model.Place
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun VacationDetailsCard(
    places: List<Place>,
    creatorUsername: String,
    createdAt: String,
    onPlaceClick: (Place) -> Unit,
    onRemovePlace: (Place) -> Unit,
    isOwner: Boolean,
    modifier: Modifier = Modifier,
    isSaved: Boolean? = null,
    onSaveToggle: (() -> Unit)? = null,
    isSavingInProgress: Boolean = false,
    getUserRating: (String) -> Pair<Double?, Int>,
    showMetadata: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = AmaranthPurple
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showMetadata) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Created by $creatorUsername",
                                fontFamily = Raleway,
                                fontSize = 14.sp,
                                color = Lavender
                            )
                            Text(
                                text = DateFormatter.formatToShort(createdAt),
                                fontFamily = Raleway,
                                fontSize = 14.sp,
                                color = Lavender
                            )
                        }

                        if (!isOwner) {
                            if (onSaveToggle != null) {
                                IconButton(
                                    onClick = onSaveToggle,
                                    enabled = !isSavingInProgress
                                ) {
                                    if (isSavingInProgress) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Lavender,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (isSaved == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = if (isSaved == true) stringResource(id = R.string.unsave_vacation) else stringResource(id = R.string.save_vacation),
                                            tint = Lavender,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = Lavender,
                    thickness = 1.dp
                )
            }

            if (places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_places_in_vacation),
                        fontFamily = Raleway,
                        fontSize = 16.sp,
                        color = Lavender.copy(alpha = 0.7f)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    places.forEach { place ->
                        val (userRating, userReviewsCount) = getUserRating(place.id)

                        PlaceCard(
                            place = place,
                            onCardClick = { onPlaceClick(place) },
                            showRemoveButton = isOwner,
                            onRemoveClick = { onRemovePlace(place) },
                            userRating = userRating,
                            userReviewsCount = userReviewsCount
                        )
                    }
                }
            }
        }
    }
}