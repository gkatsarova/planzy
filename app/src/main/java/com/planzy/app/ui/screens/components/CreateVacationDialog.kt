package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.planzy.app.R
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway

@Composable
fun CreateVacationDialog(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Lavender,
        title = {
            Text(
                text = stringResource(id = R.string.new_vacation),
                fontFamily = Raleway,
                color = AmericanBlue
            )
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.vacation_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MediumBluePurple,
                    unfocusedBorderColor = AmericanBlue,
                    focusedLabelColor = MediumBluePurple,
                    unfocusedLabelColor = AmericanBlue,
                    cursorColor = MediumBluePurple
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title) },
                enabled = title.isNotBlank() && !isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediumBluePurple,
                    contentColor = Lavender,
                    disabledContainerColor = MediumBluePurple.copy(alpha = 0.5f),
                    disabledContentColor = Lavender.copy(alpha = 0.5f)
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Lavender,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(id = R.string.create))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text(
                    stringResource(id = R.string.cancel),
                    color = AmericanBlue
                )
            }
        }
    )
}