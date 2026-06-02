package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.UpdateUserCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdateUserCommentUseCaseTest {

    private lateinit var repository: PlacesRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: UpdateUserCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        resourceProvider = mockk()
        useCase = UpdateUserCommentUseCase(repository, resourceProvider)

        every { resourceProvider.getString(R.string.empty_comment_text) } returns "Empty text"
        every { resourceProvider.getString(R.string.rating_error) } returns "Invalid rating"
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `update valid comment returns success`() = runTest {
        coEvery { repository.updateUserComment("id", "Updated", 4) } returns Result.success(Unit)

        val result = useCase("id", "Updated", 4)

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `update blank text returns failure`() = runTest {
        val result = useCase("id", "   ", 5)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty text", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.updateUserComment(any(), any(), any()) }
    }

    @Test
    fun `update invalid rating returns failure`() = runTest {
        val result = useCase("id", "Good", 0)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Invalid rating", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.updateUserComment(any(), any(), any()) }
    }
}