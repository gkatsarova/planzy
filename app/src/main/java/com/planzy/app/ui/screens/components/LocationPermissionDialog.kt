package com.planzy.app.ui.screens.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.planzy.app.R
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway

@Composable
fun LocationPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPermissionResult: (granted: Boolean) -> Unit
) {
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        onPermissionResult(fineLocationGranted || coarseLocationGranted)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MediumBluePurple,
            title = {
                Text(
                    text = stringResource(id = R.string.location_permission_title),
                    fontFamily = Raleway,
                    color = Lavender
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.location_permission_message),
                    fontFamily = Raleway,
                    color = Lavender
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.allow),
                        fontFamily = Raleway,
                        color = Lavender
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = stringResource(id = R.string.deny),
                        fontFamily = Raleway,
                        color = Lavender
                    )
                }
            }
        )
    }
}