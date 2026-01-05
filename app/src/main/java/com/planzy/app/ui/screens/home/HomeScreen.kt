package com.planzy.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.repository.PlacesRepositoryImpl
import com.planzy.app.ui.screens.components.PlaceCard
import com.planzy.app.ui.screens.components.PlanzyTopAppBar
import com.planzy.app.ui.theme.Raleway

@Composable
fun HomeScreen(navController: NavController) {

    val api = remember { TripadvisorApi() }
    val repository = remember { PlacesRepositoryImpl(api) }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(repository = repository)
    )
    Scaffold(
        topBar = {
            PlanzyTopAppBar(
                title = "Home",
                navController = navController,
                onSearch = { query ->
                    viewModel.searchForPlaces(query)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.places.isEmpty()) {
                Text(
                    text = "Home",
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = Raleway
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.places) { place ->
                        PlaceCard(
                            place = place,
                            onCardClick = { }
                        )
                    }
                }
            }
        }
    }
}