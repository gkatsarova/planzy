package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Lavender.copy(alpha = 0.6f),
                fontFamily = Raleway
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Lavender,
                fontFamily = Raleway
            )
        }
    }
}