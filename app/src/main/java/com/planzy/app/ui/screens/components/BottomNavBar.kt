package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.planzy.app.ui.navigation.Home
import com.planzy.app.ui.navigation.VacationPlanner
import com.planzy.app.R
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(10.dp)),
        color = AmaranthPurple
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.height(70.dp)
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home),
                        contentDescription = stringResource(id = R.string.home),
                        modifier = Modifier.size(40.dp),
                        tint = Lavender
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.home),
                        color = Lavender,
                        fontFamily = Raleway,
                        fontSize = 12.sp
                    )
                },
                selected = currentRoute == Home.route,
                onClick = {
                    if (currentRoute != Home.route) {
                        navController.navigate(Home.route) {
                            popUpTo(Home.route) { inclusive = true }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_planner),
                        contentDescription = stringResource(id = R.string.planner),
                        modifier = Modifier.size(40.dp),
                        tint = Lavender
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.planner),
                        color = Lavender,
                        fontFamily = Raleway,
                        fontSize = 12.sp
                    )
                },
                selected = currentRoute == VacationPlanner.route,
                onClick = {
                    if (currentRoute != VacationPlanner.route) {
                        navController.navigate(VacationPlanner.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}