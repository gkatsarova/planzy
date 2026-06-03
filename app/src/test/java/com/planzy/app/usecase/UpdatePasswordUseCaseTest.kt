package com.planzy.app.usecase

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.UpdatePasswordUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdatePasswordUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: UpdatePasswordUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        useCase = UpdatePasswordUseCase(authRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `failed password update returns error message`() = runTest {
        coEvery { authRepository.updatePassword(any()) } returns
                Result.failure(Exception("Update failed"))

        val result = useCase("NewPassword123!")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Update failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `repository exception returns error message`() = runTest {
        coEvery { authRepository.updatePassword(any()) } returns
                Result.failure(AppException(AppError.ERROR_UPDATE_PASSWORD))

        val result = useCase("NewPassword123!")

        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is AppException)
        Assert.assertEquals(
            AppError.ERROR_UPDATE_PASSWORD,
            (result.exceptionOrNull() as AppException).error
        )
    }
}