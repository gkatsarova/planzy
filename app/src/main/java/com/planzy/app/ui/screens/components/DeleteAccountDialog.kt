package com.planzy.app.ui.screens.components

import androidx.compose.material3.AlertDialog
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
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.delete_account_title),
                fontFamily = Raleway,
                fontWeight = FontWeight.Bold,
                color = Lavender
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.delete_account_message),
                fontFamily = Raleway,
                color = Lavender
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(id = R.string.delete),
                    fontFamily = Raleway,
                    color = ErrorColor
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    fontFamily = Raleway,
                    color = Lavender
                )
            }
        },
        containerColor = MediumBluePurple
    )
}