package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.planzy.app.ui.navigation.Profile
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.R
import com.planzy.app.ui.theme.Raleway

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanzyTopAppBar(
    title: String,
    navController: NavController,
    onSearch: (String) -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit = {}
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AmaranthPurple,
            shadowElevation = 4.dp
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
                            searchQuery = ""
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
                            onQueryChange = { searchQuery = it },
                            onSearch = { onSearch(searchQuery) },
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

                        IconButton(onClick = {
                            navController.navigate(Profile.route)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.user_profile_pic_placeholder),
                                contentDescription = stringResource(id = R.string.profile),
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}