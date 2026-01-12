package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.ui.theme.*

@Composable
fun ReviewCard(
    review: PlaceReview,
    modifier: Modifier = Modifier
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.author,
                    fontFamily = Raleway,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Lavender
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
                            tint = if (index < review.rating) AmaranthPurple else Lavender
                        )
                    }
                }
            }

            review.text?.let { text ->
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        fontFamily = Raleway,
                        fontSize = 14.sp,
                        color = Lavender
                    )
                }
            }

            Text(
                text = review.date,
                fontFamily = Raleway,
                fontSize = 12.sp,
                color = Lavender
            )
        }
    }
}