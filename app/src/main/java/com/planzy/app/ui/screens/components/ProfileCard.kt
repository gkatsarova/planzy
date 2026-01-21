package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.R
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.MediumBluePurple
import com.planzy.app.ui.theme.Raleway

@Composable
fun ProfileCard(
    username: String,
    email: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MediumBluePurple
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileInfoRow(
                label = stringResource(id = R.string.username),
                value = username
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoRow(
                label = stringResource(id = R.string.email),
                value = email
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onLogoutClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(id = R.string.logout),
                        tint = Lavender,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = stringResource(id = R.string.logout),
                    fontFamily = Raleway,
                    fontSize = 12.sp,
                    color = Lavender
                )
            }
        }
    }
}