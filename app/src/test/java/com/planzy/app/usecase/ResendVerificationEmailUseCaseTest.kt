package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.ResendVerificationEmailUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ResendVerificationEmailUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: ResendVerificationEmailUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        resourceProvider = mockk()
        useCase = ResendVerificationEmailUseCase(authRepository, resourceProvider)

        every { resourceProvider.getString(R.string.success_resend_verification_email) } returns
                "Verification email sent"
        every { resourceProvider.getString(R.string.error_verification_email_resend) } returns
                "Failed to resend email"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `successful resend returns success message`() = runTest {
        coEvery { authRepository.resendVerificationEmail("user@example.com") } returns
                Result.success(Unit)

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("Verification email sent", result.getOrNull())
    }

    @Test
    fun `failed resend returns error message`() = runTest {
        coEvery { authRepository.resendVerificationEmail(any()) } returns
                Result.failure(Exception())

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Failed to resend email", result.exceptionOrNull()?.message)
    }
}