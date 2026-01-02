package com.planzy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.planzy.app.data.repository.DeepLinkHandler
import com.planzy.app.data.util.RecoverySessionManager
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.navigation.Navigation
import com.planzy.app.ui.screens.auth.registration.DeepLinkViewModel
import com.planzy.app.ui.theme.PlanzyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()
    private val resourceProvider by lazy { ResourceProviderImpl(this) }
    private val recoverySessionManager by lazy { RecoverySessionManager(this) }
    private val deepLinkHandler by lazy { DeepLinkHandler(resourceProvider, recoverySessionManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLink(intent)

        setContent {
            PlanzyTheme {
                Navigation(deepLinkViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data == null) {
            return
        }
        lifecycleScope.launch {
            val result = deepLinkHandler.handleAuthDeepLink(intent)

            deepLinkViewModel.handleDeepLinkResult(result)
        }
    }
}