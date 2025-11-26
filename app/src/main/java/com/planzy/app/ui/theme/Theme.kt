package com.planzy.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AmaranthPurple,
    secondary = MediumBluePurple,
    background = Lavender,
    onBackground = AmericanBlue
)

@Composable
fun PlanzyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}