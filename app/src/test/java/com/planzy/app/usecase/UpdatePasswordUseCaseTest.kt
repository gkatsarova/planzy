package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.UpdatePasswordUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdatePasswordUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: UpdatePasswordUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        resourceProvider = mockk()
        useCase = UpdatePasswordUseCase(authRepository, resourceProvider)

        every { resourceProvider.getString(R.string.success_password_updated) } returns
                "Password updated successfully"
        every { resourceProvider.getString(R.string.error_update_password) } returns
                "Failed to update password"
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `successful password update returns success message`() = runTest {
        coEvery { authRepository.updatePassword("NewPassword123!") } returns
                Result.success(Unit)

        val result = useCase("NewPassword123!")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("Password updated successfully", result.getOrNull())
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
                Result.failure(Exception())

        val result = useCase("NewPassword123!")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Failed to update password", result.exceptionOrNull()?.message)
    }
}