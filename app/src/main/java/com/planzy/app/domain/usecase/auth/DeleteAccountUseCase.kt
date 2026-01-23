package com.planzy.app.domain.usecase.auth

import com.planzy.app.domain.repository.AuthRepository

class DeleteAccountUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.deleteAccount()
    }
}