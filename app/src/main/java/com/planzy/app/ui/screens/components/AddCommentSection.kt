package com.planzy.app.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.ui.theme.*
import com.planzy.app.R

@Composable
fun AddCommentSection(
    modifier: Modifier = Modifier,
    onSubmit: (text: String, rating: Int) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    initialText: String = "",
    initialRating: Int = 5,
    onCancel: (() -> Unit)? = null,
    buttonText: String = stringResource(id = R.string.post_comment)
) {
    var commentText by remember { mutableStateOf(initialText) }
    var selectedRating by remember { mutableIntStateOf(initialRating) }
    var isExpanded by remember { mutableStateOf(initialText.isNotEmpty()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MediumBluePurple,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                TextField(
                    value = commentText,
                    onValueChange = {
                        if (it.length <= 500) {
                            commentText = it
                            if (it.isNotEmpty()) isExpanded = true
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.write_comment),
                            color = Lavender,
                            fontFamily = Raleway,
                            fontSize = 16.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) isExpanded = true
                        },
                    enabled = !isSubmitting,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Lavender,
                        focusedTextColor = Lavender,
                        unfocusedTextColor = Lavender
                    )
                )

                AnimatedVisibility(
                    visible = isExpanded || commentText.isNotEmpty(),
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Lavender
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                repeat(5) { index ->
                                    IconButton(
                                        onClick = { selectedRating = index + 1 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (index < selectedRating)
                                                Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = if (index < selectedRating) Lavender else AmaranthPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onCancel != null) {
                                    TextButton(
                                        onClick = onCancel,
                                        enabled = !isSubmitting,
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Lavender
                                        ),
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.cancel),
                                            fontFamily = Raleway,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (commentText.isNotBlank()) {
                                            onSubmit(commentText, selectedRating)
                                            if (onCancel == null) {
                                                commentText = ""
                                                selectedRating = 5
                                                isExpanded = false
                                            }
                                        }
                                    },
                                    enabled = !isSubmitting && commentText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmaranthPurple,
                                        disabledContainerColor = AmaranthPurple
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Lavender,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = buttonText,
                                            fontFamily = Raleway,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = ErrorColor,
                fontSize = 12.sp,
                fontFamily = Raleway,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }
    }
}