package com.planzy.app.ui.screens.components

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
import androidx.navigation.NavController
import com.planzy.app.R
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.theme.*

@Composable
fun VacationCommentsSection(
    comments: List<VacationComment>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onEditComment: (commentId: String, text: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    onEditStart: () -> Unit,
    onEditCancel: () -> Unit,
    modifier: Modifier = Modifier,
    getUserByAuthIdUseCase: GetUserByAuthIdUseCase,
    navController: NavController
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
                    text = stringResource(R.string.no_comments_yet),
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
                        VacationCommentCard(
                            comment = comment,
                            onEdit = onEditComment,
                            onDelete = onDeleteComment,
                            onEditStart = onEditStart,
                            onEditCancel = onEditCancel,
                            getUserByAuthIdUseCase = getUserByAuthIdUseCase,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}