package com.planzy.app.ui.screens.auth.registration

import androidx.lifecycle.ViewModel
import com.planzy.app.data.repository.DeepLinkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeepLinkViewModel : ViewModel() {

    private val _deepLinkResult = MutableStateFlow<DeepLinkResult>(DeepLinkResult.NoDeepLink)
    val deepLinkResult: StateFlow<DeepLinkResult> = _deepLinkResult

    private val _lastRoute = MutableStateFlow<String?>(null)

    private val _pendingEmail = MutableStateFlow<String?>(null)

    private val _pendingPassword = MutableStateFlow<String?>(null)

    fun handleDeepLinkResult(result: DeepLinkResult) {
        _deepLinkResult.value = result
    }

    fun clearDeepLinkResult() {
        _deepLinkResult.value = DeepLinkResult.NoDeepLink
    }

    fun saveLastRoute(route: String) {
        _lastRoute.value = route
    }

    fun savePendingCredentials(email: String, password: String) {
        _pendingEmail.value = email
        _pendingPassword.value = password
    }

    fun getPendingCredentials(): Pair<String?, String?> {
        return Pair(_pendingEmail.value, _pendingPassword.value)
    }

    fun clearPendingCredentials() {
        _pendingEmail.value = null
        _pendingPassword.value = null
    }
}