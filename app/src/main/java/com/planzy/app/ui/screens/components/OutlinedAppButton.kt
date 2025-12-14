package com.planzy.app.ui.screens.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.Raleway

@Composable
fun OutlinedAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = "Loading...",
    fontSize: TextUnit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = AmaranthPurple,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = AmericanBlue
        ),
        border = BorderStroke(
            width = 2.dp,
            color = AmaranthPurple
        ),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled
    ) {
        if (loading && !enabled) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AmaranthPurple
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = loadingText,
                fontFamily = Raleway,
                fontSize = fontSize
            )
        } else {
            Text(
                text = text,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                fontFamily = Raleway
            )
        }
    }
}