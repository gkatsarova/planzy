package com.planzy.app.domain.usecase.auth

import com.planzy.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.user.UserInfo

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): UserInfo? {
        return authRepository.getCurrentUser()
    }
}