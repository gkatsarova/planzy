package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.AddVacationCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AddVacationCommentUseCaseTest {

    private lateinit var repository: VacationsRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: AddVacationCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        resourceProvider = mockk()
        useCase = AddVacationCommentUseCase(repository, resourceProvider)

        every { resourceProvider.getString(R.string.empty_comment_text) } returns "Empty comment text"
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `valid comment returns success`() = runTest {
        val comment = mockk<VacationComment>()
        coEvery { repository.addVacationComment("vacation123", "Great vacation!" ) } returns Result.success(comment)

        val result = useCase("vacation123", "Great vacation!")

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(comment, result.getOrNull())
    }

    @Test
    fun `blank text returns failure`() = runTest {
        val result = useCase("vacation123", "  ")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty comment text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `empty text returns failure`() = runTest {
        val result = useCase("vacation123", "")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty comment text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `text with only whitespace returns failure`() = runTest {
        val result = useCase("vacation123", "   \n   ")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty comment text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        val exception = Exception("Database error")
        coEvery { repository.addVacationComment("vacation123", "Nice trip") } returns Result.failure(exception)

        val result = useCase("vacation123", "Nice trip")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}