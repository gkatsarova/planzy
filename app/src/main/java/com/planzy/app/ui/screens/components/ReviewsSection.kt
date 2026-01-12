package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.R
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.ui.theme.*

@Composable
fun ReviewsSection(
    reviews: List<PlaceReview>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.reviews_title),
            fontFamily = Raleway,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AmericanBlue
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MediumBluePurple)
                }
            }

            reviews.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.no_reviews),
                    fontFamily = Raleway,
                    fontSize = 16.sp,
                    color = AmericanBlue
                )
            }

            else -> {
                reviews.forEach { review ->
                    ReviewCard(review = review)
                }
            }
        }
    }
}