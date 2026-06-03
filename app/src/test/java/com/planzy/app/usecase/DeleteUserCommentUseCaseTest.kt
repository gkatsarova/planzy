package com.planzy.app.usecase


import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.DeleteUserCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeleteUserCommentUseCaseTest {

    private lateinit var repository: PlacesRepository
    private lateinit var useCase: DeleteUserCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteUserCommentUseCase(repository)
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `delete comment calls repository`() = runTest {
        coEvery { repository.deleteUserComment("id") } returns Result.success(Unit)

        val result = useCase("id")

        Assert.assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteUserComment("id") }
    }
}