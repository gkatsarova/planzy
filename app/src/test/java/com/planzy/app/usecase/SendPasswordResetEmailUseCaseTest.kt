package com.planzy.app.usecase

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SendPasswordResetEmailUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SendPasswordResetEmailUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        useCase = SendPasswordResetEmailUseCase(authRepository)
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
        Assert.assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `failed email send returns error message`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail(any()) } returns
                Result.failure(AppException(AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED))

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        Assert.assertNotNull(exception)
        Assert.assertTrue(exception is AppException)
        Assert.assertEquals(AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED, (exception as AppException).error)
    }

    @Test
    fun `repository exception returns error message`() = runTest {
        coEvery { authRepository.sendPasswordResetEmail(any()) } returns
                Result.failure(AppException(AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED))

        val result = useCase("user@example.com")

        Assert.assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        Assert.assertNotNull(exception)
        Assert.assertTrue(exception is AppException)
        Assert.assertEquals(AppError.ERROR_PASSWORD_RESET_EMAIL_FAILED, (exception as AppException).error)
    }
}