package com.planzy.app.usecase

import com.planzy.app.data.model.User
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.usecase.CheckUsernameAvailabilityUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CheckUsernameAvailabilityUseCaseTest {
    private lateinit var userRepository: UserRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: CheckUsernameAvailabilityUseCase

    @Before
    fun setup() {
        userRepository = mockk()
        resourceProvider = mockk()
        useCase = CheckUsernameAvailabilityUseCase(userRepository, resourceProvider)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `available username returns true`() = runTest {
        coEvery { userRepository.getUserByUsername("newuser") } returns Result.success(null)

        val result = useCase("newuser")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(true, result.getOrNull())
    }

    @Test
    fun `existing username returns false`() = runTest {
        val existingUser = User(
            id = 123,
            email = "existing@example.com",
            username = "existinguser",
            auth_id = "auth123"
        )
        coEvery { userRepository.getUserByUsername("existinguser") } returns
                Result.success(existingUser)

        val result = useCase("existinguser")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(false, result.getOrNull())
    }
}