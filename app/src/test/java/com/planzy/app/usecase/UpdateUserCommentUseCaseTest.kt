package com.planzy.app.usecase

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.UpdateUserCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdateUserCommentUseCaseTest {

    private lateinit var repository: PlacesRepository
    private lateinit var useCase: UpdateUserCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateUserCommentUseCase(repository)
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
        Assert.assertEquals(
            AppError.EMPTY_COMMENT_TEXT,
            (result.exceptionOrNull() as? AppException)?.error
        )
        coVerify(exactly = 0) { repository.updateUserComment(any(), any(), any()) }
    }

    @Test
    fun `update invalid rating returns failure`() = runTest {
        val result = useCase("id", "Good", 0)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(
            AppError.RATING_ERROR,
            (result.exceptionOrNull() as? AppException)?.error
        )
        coVerify(exactly = 0) { repository.updateUserComment(any(), any(), any()) }
    }
}