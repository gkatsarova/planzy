package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.UpdateVacationCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
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
    fun `valid update returns success`() = runTest {
        coEvery { repository.updateVacationComment("comment123", "Updated text") } returns Result.success(Unit)

        val result = useCase("comment123", "Updated text")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `blank text returns failure`() = runTest {
        val result = useCase("comment123", "  ")

        assertTrue(result.isFailure)
        assertEquals("Empty comment text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `empty text returns failure`() = runTest {
        val result = useCase("comment123", "")

        assertTrue(result.isFailure)
        assertEquals("Empty comment text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `text with only whitespace returns failure`() = runTest {
        val result = useCase("comment123", "   \n   \t   ")

        assertTrue(result.isFailure)
        assertEquals("Empty comment text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        val exception = Exception("Permission denied")
        coEvery { repository.updateVacationComment("comment123", "New text") } returns Result.failure(exception)

        val result = useCase("comment123", "New text")

        assertTrue(result.isFailure)
        assertEquals("Permission denied", result.exceptionOrNull()?.message)
    }

    @Test
    fun `text with leading and trailing spaces is accepted`() = runTest {
        coEvery { repository.updateVacationComment("comment123", "  Updated  ") } returns Result.success(Unit)

        val result = useCase("comment123", "  Updated  ")

        assertTrue(result.isSuccess)
    }
}