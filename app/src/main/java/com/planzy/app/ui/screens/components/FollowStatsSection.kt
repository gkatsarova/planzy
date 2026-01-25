package com.planzy.app.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planzy.app.R
import com.planzy.app.domain.model.FollowStats
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun FollowStatsSection(
    followStats: FollowStats?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isToggling: Boolean = false,
    onFollowClick: () -> Unit = {},
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    showFollowButton: Boolean = true
) {
    if (followStats == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    enabled = !isLoading && !isToggling,
                    onClick = onFollowersClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = followStats.followersCount.toString(),
                fontSize = 16.sp,
                fontFamily = Raleway,
                color = AmericanBlue
            )
            Text(
                text = stringResource(id = R.string.followers),
                fontSize = 16.sp,
                fontFamily = Raleway,
                color = AmericanBlue
            )
        }

        if (showFollowButton) {
            if (isLoading || isToggling) {
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AmaranthPurple
                    )
                }
            } else {
                if (followStats.isFollowing) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        modifier = Modifier,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AmericanBlue
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.unfollow),
                            fontSize = 16.sp,
                            fontFamily = Raleway
                        )
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmaranthPurple
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.follow),
                            fontSize = 16.sp,
                            fontFamily = Raleway,
                            color = Lavender
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    enabled = !isLoading && !isToggling,
                    onClick = onFollowingClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = followStats.followingCount.toString(),
                fontSize = 16.sp,
                fontFamily = Raleway,
                color = AmericanBlue
            )
            Text(
                text = stringResource(id = R.string.following),
                fontSize = 16.sp,
                fontFamily = Raleway,
                color = AmericanBlue
            )
        }
    }
}