package com.planzy.app.ui.screens.auth.registration

import androidx.lifecycle.ViewModel
import com.planzy.app.data.repository.DeepLinkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeepLinkViewModel : ViewModel() {

    private val _deepLinkResult = MutableStateFlow<DeepLinkResult>(DeepLinkResult.NoDeepLink)
    val deepLinkResult: StateFlow<DeepLinkResult> = _deepLinkResult

    fun handleDeepLinkResult(result: DeepLinkResult) {
        _deepLinkResult.value = result
    }

    fun clearDeepLinkResult() {
        _deepLinkResult.value = DeepLinkResult.NoDeepLink
    }
}