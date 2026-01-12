package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.domain.model.UserComment
import com.planzy.app.ui.theme.*
import com.planzy.app.R


@Composable
fun UserCommentsSection(
    comments: List<UserComment>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            isLoading && comments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmaranthPurple)
                }
            }

            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = errorMessage, color = ErrorColor, fontSize = 14.sp)
                        TextButton(onClick = onRetry) {
                            Text(
                                text = stringResource(id = R.string.retry),
                                color = AmaranthPurple
                            )
                        }
                    }
                }
            }

            comments.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.no_reviews_yet),
                    fontFamily = Raleway,
                    fontSize = 15.sp,
                    color = AmericanBlue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            else -> {
                comments.forEach { comment ->
                    UserCommentCard(comment = comment)
                }
            }
        }
    }
}