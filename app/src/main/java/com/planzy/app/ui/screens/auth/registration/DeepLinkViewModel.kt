package com.planzy.app.ui.screens.auth.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.planzy.app.data.repository.DeepLinkResult

class DeepLinkViewModel : ViewModel() {

    var deepLinkResult by mutableStateOf<DeepLinkResult>(DeepLinkResult.NoDeepLink)
        private set

    private var lastRoute by mutableStateOf<String?>(null)

    private var pendingEmail by mutableStateOf<String?>(null)

    private var pendingPassword by mutableStateOf<String?>(null)

    fun handleDeepLinkResult(result: DeepLinkResult) {
        deepLinkResult = result
    }

    fun clearDeepLinkResult() {
        deepLinkResult = DeepLinkResult.NoDeepLink
    }

    fun saveLastRoute(route: String) {
        lastRoute = route
    }

    fun savePendingCredentials(email: String, password: String) {
        pendingEmail = email
        pendingPassword = password
    }

    fun getPendingCredentials(): Pair<String?, String?> {
        return Pair(pendingEmail, pendingPassword)
    }

    fun clearPendingCredentials() {
        pendingEmail = null
        pendingPassword = null
    }
}