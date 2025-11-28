package com.planzy.app.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.planzy.app.R
import com.planzy.app.ui.screens.components.WelcomeButton
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.navigation.Register

@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        Image(
            painter = painterResource(id = R.drawable.welcome_screen_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(height = 80.dp))

            Text(
                text = stringResource(id = R.string.welcome),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 38.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.welcome_message),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(height = 500.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WelcomeButton(
                    text = stringResource(id = R.string.login),
                    onClick = { navController.navigate(route = Login.route) },
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(10.dp)
                        .height(70.dp)
                )

                WelcomeButton(
                    text = stringResource(id = R.string.register),
                    onClick = { navController.navigate(route = Register.route) },
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(10.dp)
                        .height(70.dp)
                )
            }
        }
    }
}