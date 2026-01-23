package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.planzy.app.ui.navigation.Profile
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.R
import com.planzy.app.ui.theme.Raleway

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanzyTopAppBar(
    title: String,
    profilePictureUrl: String?,
    navController: NavController,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit = {}
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchQuery) {
        localSearchQuery = searchQuery
        if (searchQuery.isEmpty()) {
            isSearchActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AmaranthPurple
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AmaranthPurple,
                    titleContentColor = Lavender,
                    actionIconContentColor = Lavender,
                    navigationIconContentColor = Lavender
                ),
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            localSearchQuery = ""
                            onSearch("")
                            onSearchFocusChanged(false)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.close_search)
                            )
                        }
                    }
                },
                title = {
                    if (isSearchActive) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                localSearchQuery = it
                                onSearch(it)
                            },
                            onSearch = { onSearch(localSearchQuery) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .focusRequester(focusRequester)
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        Text(
                            text = title,
                            fontFamily = Raleway,
                            fontSize = 24.sp
                        )
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = true
                            onSearchFocusChanged(true)}) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = stringResource(id = R.string.search),
                                modifier = Modifier.height(30.dp)
                            )
                        }

                        IconButton(onClick = { navController.navigate(Profile.route) }) {
                            if (profilePictureUrl != null) {
                                AsyncImage(
                                    model = profilePictureUrl,
                                    contentDescription = stringResource(id = R.string.profile),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(30.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.user_profile_pic_placeholder),
                                    contentDescription = stringResource(id = R.string.profile),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(30.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}