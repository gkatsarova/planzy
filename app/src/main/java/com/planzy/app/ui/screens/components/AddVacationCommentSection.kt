package com.planzy.app.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun AddVacationCommentSection(
    modifier: Modifier = Modifier,
    onSubmit: (text: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    initialText: String = "",
    onCancel: (() -> Unit)? = null,
    buttonText: String = stringResource(id = R.string.post_comment)
) {
    var commentText by remember { mutableStateOf(initialText) }
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
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
                                        onSubmit(commentText)
                                        if (onCancel == null) {
                                            commentText = ""
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
                                        color = Lavender,
                                        maxLines = 1
                                    )
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