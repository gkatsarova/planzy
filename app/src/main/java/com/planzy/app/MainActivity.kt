package com.planzy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.planzy.app.data.DeepLinkHandler
import com.planzy.app.ui.navigation.Navigation
import com.planzy.app.ui.screens.registration.DeepLinkViewModel
import com.planzy.app.ui.theme.PlanzyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLink(intent)

        setContent {
            PlanzyTheme {
                Navigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        lifecycleScope.launch {
            val result = DeepLinkHandler.handleAuthDeepLink(intent)

            deepLinkViewModel.handleDeepLinkResult(result)
        }
    }
}