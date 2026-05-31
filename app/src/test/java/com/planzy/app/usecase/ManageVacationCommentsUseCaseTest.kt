package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.ManageVacationCommentsUseCase
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

class ManageVacationCommentsUseCaseTest {

    private lateinit var repository: VacationsRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: ManageVacationCommentsUseCase

    @Before
    fun setup() {
        repository = mockk()
        resourceProvider = mockk()
        useCase = ManageVacationCommentsUseCase(repository, resourceProvider)

        every { resourceProvider.getString(R.string.empty_comment_text) } returns "Empty comment text"
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `add valid comment returns success`() = runTest {
        val comment = mockk<VacationComment>()
        coEvery { repository.addVacationComment("vacation123", "Great vacation!") } returns Result.success(comment)

        val result = useCase.addComment("vacation123", "Great vacation!")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(comment, result.getOrNull())
    }

    @Test
    fun `add blank text returns failure`() = runTest {
        val result = useCase.addComment("vacation123", "  ")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty comment text", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.addVacationComment(any(), any()) }
    }

    @Test
    fun `update valid comment returns success`() = runTest {
        coEvery { repository.updateVacationComment("comment123", "Updated comment") } returns Result.success(Unit)

        val result = useCase.updateComment("comment123", "Updated comment")

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `update blank text returns failure`() = runTest {
        val result = useCase.updateComment("comment123", "   ")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty comment text", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.updateVacationComment(any(), any()) }
    }

    @Test
    fun `delete comment returns success`() = runTest {
        coEvery { repository.deleteVacationComment("comment123") } returns Result.success(Unit)

        val result = useCase.deleteComment("comment123")

        Assert.assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteVacationComment("comment123") }
    }
}
