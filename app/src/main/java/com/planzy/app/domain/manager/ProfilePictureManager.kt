package com.planzy.app.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProfilePictureManager {
    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl.asStateFlow()

    fun updateUrl(url: String?) {
        _profilePictureUrl.value = url
    }
}