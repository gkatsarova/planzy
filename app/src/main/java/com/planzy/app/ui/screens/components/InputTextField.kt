package com.planzy.app.ui.screens.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor

@Composable
fun InputTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    modifier: Modifier
) {
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
        isError = isError,
        singleLine = true,
        modifier = modifier
    )
}