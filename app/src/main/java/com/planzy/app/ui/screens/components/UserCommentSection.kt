package com.planzy.app.ui.screens.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.planzy.app.R
import com.planzy.app.domain.model.UserComment
import com.planzy.app.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserCommentsSection(
    comments: List<UserComment>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onEditComment: (commentId: String, text: String, rating: Int) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmaranthPurple)
                }
            }

            errorMessage != null -> {
                RetrySection(
                    errorMessage = errorMessage,
                    onRetry = onRetry
                )
            }

            comments.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_reviews_yet),
                    fontFamily = Raleway,
                    color = AmericanBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(comments) { comment ->
                        UserCommentCard(
                            comment = comment,
                            onEdit = onEditComment,
                            onDelete = onDeleteComment
                        )
                    }
                }
            }
        }
    }
}