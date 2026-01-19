package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
fun ChatBubble(message: ChatMessage) {
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start

    val bubbleColor = if (message.isUser) MediumBluePurple else AmericanBlue
    val textColor = Lavender

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = if (message.isUser) 10.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 10.dp
            ),
            color = bubbleColor
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(12.dp)
            )

        }
    }
}