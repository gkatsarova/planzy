package com.planzy.app.ui.screens.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.planzy.app.R
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor

@Composable
fun PasswordTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    isError: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = value,
        label = { Text(text = label) },
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AmaranthPurple,
            unfocusedBorderColor = AmericanBlue,
            focusedLabelColor = AmaranthPurple,
            unfocusedLabelColor = AmericanBlue,
            focusedTextColor = AmaranthPurple,
            unfocusedTextColor = AmericanBlue,
            errorBorderColor = ErrorColor,
            errorLabelColor = ErrorColor,
            errorTextColor = ErrorColor,
            cursorColor = AmaranthPurple,
            errorCursorColor = ErrorColor
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        isError = isError,
        singleLine = true,
        interactionSource = interactionSource,
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible)
                        stringResource(id = R.string.hide_password)
                    else
                        stringResource(id = R.string.show_password),
                    tint = when {
                        isError -> ErrorColor
                        isFocused -> AmaranthPurple
                        else -> AmericanBlue
                    }
                )
            }
        }
    )
}