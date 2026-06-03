package com.planzy.app.usecase

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.AddUserCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AddUserCommentUseCaseTest {

    private lateinit var repository: PlacesRepository
    private lateinit var useCase: AddUserCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = AddUserCommentUseCase(repository)
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `add valid comment returns success`() = runTest {
        val comment = mockk<UserComment>()
        coEvery { repository.addUserComment("id", "Cool place", 5) } returns Result.success(comment)

        val result = useCase("id", "Cool place", 5)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(comment, result.getOrNull())
    }

    @Test
    fun `add blank text returns failure`() = runTest {
        val result = useCase("id", "  ", 5)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(
            AppError.EMPTY_COMMENT_TEXT,
            (result.exceptionOrNull() as? AppException)?.error
        )
        coVerify(exactly = 0) { repository.addUserComment(any(), any(), any()) }
    }

    @Test
    fun `add invalid rating returns failure`() = runTest {
        val result = useCase("id", "Cool place", 6)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(
            AppError.RATING_ERROR,
            (result.exceptionOrNull() as? AppException)?.error
        )
        coVerify(exactly = 0) { repository.addUserComment(any(), any(), any()) }
    }
}