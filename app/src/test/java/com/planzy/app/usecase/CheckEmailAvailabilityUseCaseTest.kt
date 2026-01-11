package com.planzy.app.usecase

import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.CheckEmailAvailabilityUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CheckEmailAvailabilityUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: CheckEmailAvailabilityUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        resourceProvider = mockk()
        useCase = CheckEmailAvailabilityUseCase(authRepository, resourceProvider)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `available email returns true`() = runTest {
        coEvery { authRepository.checkEmailExistsInAuth("newuser@example.com") } returns
                Result.success(false)

        val result = useCase("newuser@example.com")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(true, result.getOrNull())
    }

    @Test
    fun `existing email returns false`() = runTest {
        coEvery { authRepository.checkEmailExistsInAuth("existing@example.com") } returns
                Result.success(true)

        val result = useCase("existing@example.com")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(false, result.getOrNull())
    }

    @Test
    fun `repository failure returns error`() = runTest {
        coEvery { authRepository.checkEmailExistsInAuth(any()) } returns
                Result.failure(Exception("Network error"))

        val result = useCase("test@example.com")

        Assert.assertTrue(result.isFailure)
    }
}