package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.ErrorColor

enum class MessageType {
    ERROR,
    SUCCESS
}

@Composable
fun MessageCard(
    message: String,
    type: MessageType,
    modifier: Modifier
) {
    val textColor = when (type) {
        MessageType.ERROR -> ErrorColor
        MessageType.SUCCESS -> AmaranthPurple
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Text(
            text = message,
            color = textColor
        )
    }
}