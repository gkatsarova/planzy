package com.planzy.app.ui.screens.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.planzy.app.R
import com.planzy.app.domain.model.Vacation
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway

@Composable
fun DeleteVacationDialog(
    vacation: Vacation,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        title = {
            Text(
                text = stringResource(id = R.string.delete_vacation),
                fontFamily = Raleway,
                fontWeight = FontWeight.Bold,
                color = Lavender
            )
        },
        text = {
            Text(
                text = stringResource(
                    id = R.string.delete_vacation_confirmation,
                    vacation.title
                ),
                fontFamily = Raleway,
                color = Lavender
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                Text(
                    text = stringResource(id = R.string.delete),
                    color = ErrorColor
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Lavender
                )
            }
        },
        containerColor = MediumBluePurple
    )
}