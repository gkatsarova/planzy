package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.UpdateVacationCommentUseCase
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

class UpdateVacationCommentUseCaseTest {

    private lateinit var repository: VacationsRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: UpdateVacationCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        resourceProvider = mockk()
        useCase = UpdateVacationCommentUseCase(repository, resourceProvider)

        every { resourceProvider.getString(R.string.empty_comment_text) } returns "Empty comment text"
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `update valid comment returns success`() = runTest {
        coEvery { repository.updateVacationComment("comment123", "Updated comment") } returns Result.success(Unit)

        val result = useCase("comment123", "Updated comment")

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `update blank text returns failure`() = runTest {
        val result = useCase("comment123", "   ")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty comment text", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.updateVacationComment(any(), any()) }
    }
}