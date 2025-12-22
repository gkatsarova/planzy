package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    loadingText: String
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AmaranthPurple,
            contentColor = Lavender,
            disabledContainerColor = if (loading) AmaranthPurple else AmericanBlue,
            disabledContentColor = Lavender
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Lavender
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = loadingText,
                color = Lavender,
                fontSize = 30.sp,
                fontFamily = Raleway
            )
        } else {
            Text(
                text = text,
                color = Lavender,
                fontSize = 30.sp,
                fontFamily = Raleway
                )
        }
    }
}