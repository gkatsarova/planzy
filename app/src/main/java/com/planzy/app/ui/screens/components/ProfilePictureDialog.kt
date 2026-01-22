package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.planzy.app.R
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway


@Composable
fun ProfileImageDialog(
    profilePictureUrl: String?,
    onDismissRequest: () -> Unit,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MediumBluePurple,
        title = {
            Text(
                text = if (profilePictureUrl == null)
                    stringResource(id = R.string.upload_profile_picture)
                else stringResource(id = R.string.edit_profile_picture),
                color = Lavender,
                fontFamily = Raleway
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUploadClick()
                    onDismissRequest()
                }
            ) {
                Text(
                    text = if (profilePictureUrl == null)
                        stringResource(id = R.string.upload)
                    else stringResource(id = R.string.change),
                    color = Lavender,
                    fontFamily = Raleway
                )
            }
        },
        dismissButton = {
            Row {
                if (profilePictureUrl != null) {
                    TextButton(
                        onClick = {
                            onDeleteClick()
                            onDismissRequest()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.remove),
                            color = ErrorColor,
                            fontFamily = Raleway
                        )
                    }
                }
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = Lavender,
                        fontFamily = Raleway
                        )
                }
            }
        }
    )
}