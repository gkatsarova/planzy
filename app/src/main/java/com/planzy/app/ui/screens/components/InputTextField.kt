package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor

@Composable
fun InputTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            label = { Text(text = label) },
            onValueChange = onValueChange,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AmaranthPurple,
                unfocusedBorderColor = AmericanBlue,
                focusedLabelColor = AmaranthPurple,
                unfocusedLabelColor = AmericanBlue,
                errorBorderColor = ErrorColor,
                errorLabelColor = ErrorColor,
                cursorColor = AmaranthPurple,
                errorCursorColor = ErrorColor
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = visualTransformation,
            isError = isError,
            singleLine = true
        )
    }
}