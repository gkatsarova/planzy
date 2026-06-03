package com.planzy.app.ui.screens.auth.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.planzy.app.R
import com.planzy.app.data.repository.DeepLinkResult
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.AppError

class DeepLinkViewModel(
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    var deepLinkResult by mutableStateOf<DeepLinkResult>(DeepLinkResult.NoDeepLink)
        private set

    private var lastRoute by mutableStateOf<String?>(null)

    private var pendingEmail by mutableStateOf<String?>(null)

    private var pendingPassword by mutableStateOf<String?>(null)

    fun handleDeepLinkResult(result: DeepLinkResult) {
        deepLinkResult = result
    }

    fun getErrorMessage(): String {
        val currentResult = deepLinkResult
        if (currentResult is DeepLinkResult.Error) {
            if (currentResult.error == AppError.ERROR_EMAIL_VERIFICATION && currentResult.emailArg != null) {
                val baseTemplate = resourceProvider.getString(R.string.error_failed_to_verify_email)
                return String.format(baseTemplate, currentResult.emailArg)
            }
            return resourceProvider.getString(R.string.error_registration_failed)
        }
        return resourceProvider.getString(R.string.error_registration_failed)
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

    class Factory(
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeepLinkViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DeepLinkViewModel(resourceProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}