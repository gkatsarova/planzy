package com.planzy.app.domain.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository

class ResendVerificationEmailUseCase(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(email: String): Result<String> {
        return try {
            val result = authRepository.resendVerificationEmail(email)
            if (result.isSuccess) {
                Result.success(resourceProvider.getString(R.string.success_resend_verification_email))
            } else {
                Result.failure(Exception(resourceProvider.getString(R.string.error_verification_email_resend)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(resourceProvider.getString(R.string.error_verification_email_resend)))
        }
    }
}