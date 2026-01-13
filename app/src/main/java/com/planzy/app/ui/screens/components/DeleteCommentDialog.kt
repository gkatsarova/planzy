package com.planzy.app.ui.screens.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.planzy.app.R
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway

@Composable
fun DeleteCommentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MediumBluePurple,
        title = {
            Text(
                text = stringResource(R.string.delete_comment),
                fontFamily = Raleway,
                fontWeight = FontWeight.Bold,
                color = Lavender
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_comment_confirmation),
                fontFamily = Raleway,
                color = Lavender
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ErrorColor
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Lavender
                )) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}