package com.planzy.app.ui.screens.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VacationDetailsCard(
    places: List<Place>,
    creatorUsername: String,
    createdAt: String,
    onPlaceClick: (Place) -> Unit,
    onRemovePlace: (Place) -> Unit,
    isOwner: Boolean,
    getUserRating: (String) -> Pair<Double?, Int>,
    modifier: Modifier = Modifier
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

            HorizontalDivider(
                color = Lavender,
                thickness = 1.dp
            )

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