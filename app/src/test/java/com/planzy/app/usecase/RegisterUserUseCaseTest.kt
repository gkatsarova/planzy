package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.RegisterUserUseCase
import io.github.jan.supabase.auth.user.UserInfo
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RegisterUserUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: RegisterUserUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        resourceProvider = mockk()
        useCase = RegisterUserUseCase(authRepository, resourceProvider)

        every { resourceProvider.getString(R.string.success_verification_email_sent) } returns
                "Verification email sent"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `successful registration returns success message`() = runTest {
        val userInfo = mockk<UserInfo> {
            every { id } returns "auth123"
        }
        coEvery { authRepository.signUp(any(), any(), any()) } returns Result.success(userInfo)

        val result = useCase("newuser@example.com", "Password123!", "newuser")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("Verification email sent", result.getOrNull())
    }

    @Test
    fun `failed signup returns error`() = runTest {
        coEvery { authRepository.signUp(any(), any(), any()) } returns
                Result.failure(Exception("Signup failed"))

        val result = useCase("test@example.com", "Password123!", "testuser")

        Assert.assertTrue(result.isFailure)
    }
}