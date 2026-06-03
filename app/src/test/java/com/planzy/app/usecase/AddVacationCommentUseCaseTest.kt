package com.planzy.app.usecase

import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.AddVacationCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AddVacationCommentUseCaseTest {

    private lateinit var repository: VacationsRepository
    private lateinit var useCase: AddVacationCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = AddVacationCommentUseCase(repository)
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `add valid comment returns success`() = runTest {
        val comment = mockk<VacationComment>()
        coEvery { repository.addVacationComment("vacation123", "Great vacation!") } returns Result.success(comment)

        val result = useCase("vacation123", "Great vacation!")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(comment, result.getOrNull())
    }

    @Test
    fun `add blank text returns failure`() = runTest {
        val result = useCase("vacation123", "  ")

        Assert.assertTrue(result.isFailure)

        val exception = result.exceptionOrNull()
        Assert.assertTrue(exception is AppException)

        Assert.assertEquals(AppError.EMPTY_COMMENT_TEXT, (exception as AppException).error)

        coVerify(exactly = 0) { repository.addVacationComment(any(), any()) }
    }
}