package com.planzy.app.usecase

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.ResendVerificationEmailUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ResendVerificationEmailUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: ResendVerificationEmailUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        useCase = ResendVerificationEmailUseCase(authRepository)
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
        Assert.assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `failed resend returns error message`() = runTest {
        coEvery { authRepository.resendVerificationEmail(any()) } returns
                Result.failure(AppException(AppError.ERROR_VERIFICATION_EMAIL_RESEND))

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(
            AppError.ERROR_VERIFICATION_EMAIL_RESEND,
            (result.exceptionOrNull() as? AppException)?.error
        )
    }
}