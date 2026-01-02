package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.SendPasswordResetEmailUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SendPasswordResetEmailUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: SendPasswordResetEmailUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        resourceProvider = mockk()
        useCase = SendPasswordResetEmailUseCase(authRepository, resourceProvider)

        every { resourceProvider.getString(R.string.password_reset_email_sent) } returns
                "Password reset email sent"
        every { resourceProvider.getString(R.string.error_password_reset_email_failed) } returns
                "Failed to send password reset email"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `successful email send returns success message`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail("user@example.com") } returns
                Result.success(Unit)

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("Password reset email sent", result.getOrNull())
    }

    @Test
    fun `failed email send returns error message`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail(any()) } returns
                Result.failure(Exception("Email send failed"))

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Email send failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `repository exception returns error message`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail(any()) } returns
                Result.failure(Exception())

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Failed to send password reset email", result.exceptionOrNull()?.message)
    }
}