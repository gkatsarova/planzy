package com.planzy.app.usecase

import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.DeleteVacationCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeleteVacationCommentUseCaseTest {

    private lateinit var repository: VacationsRepository
    private lateinit var useCase: DeleteVacationCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteVacationCommentUseCase(repository)
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `delete comment returns success`() = runTest {
        coEvery { repository.deleteVacationComment("comment123") } returns Result.success(Unit)

        val result = useCase("comment123")

        Assert.assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteVacationComment("comment123") }
    }
}