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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.planzy.app.domain.model.Place
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.R
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun PlaceCard(
    place: Place,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
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
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.name,
                    fontFamily = Raleway,
                    fontSize = 24.sp,
                    maxLines = 2,
                    color = Lavender
                )

                place.category?.let { category ->
                    Text(
                        text = category,
                        fontFamily = Raleway,
                        fontSize = 16.sp,
                        color = Lavender
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = place.location.address,
                        fontFamily = Raleway,
                        fontSize = 16.sp,
                        maxLines = 1,
                        color = Lavender
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = place.rating.toString(),
                            fontFamily = Raleway,
                            fontSize = 16.sp,
                            color = Lavender
                        )
                    }

                    Text(
                        text = "(${place.reviewsCount} reviews)",
                        fontFamily = Raleway,
                        fontSize = 16.sp,
                        color = Lavender
                    )
                }
            }
        }
    }
}