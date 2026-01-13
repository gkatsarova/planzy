package com.planzy.app.usecase

import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.UpdateUserCommentUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import com.planzy.app.R
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert

class UpdateUserCommentUseCaseTest {

    private lateinit var repository: PlacesRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: UpdateUserCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        resourceProvider = mockk()
        useCase = UpdateUserCommentUseCase(repository, resourceProvider)

        every { resourceProvider.getString(R.string.empty_comment_text) } returns "Empty"
        every { resourceProvider.getString(R.string.rating_error) } returns "Error"
    }

    @Test
    fun `blank text returns failure and doesn't call repository`() = runTest {
        val result = useCase("id", "   ", 5)

        Assert.assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateUserComment(any(), any(), any()) }
    }

    @Test
    fun `invalid rating returns failure`() = runTest {
        val result = useCase("id", "Good", 0)

        Assert.assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.updateUserComment(any(), any(), any()) }
    }

    @Test
    fun `valid update calls repository`() = runTest {
        coEvery { repository.updateUserComment("id", "Updated", 4) } returns Result.success(Unit)

        val result = useCase("id", "Updated", 4)

        Assert.assertTrue(result.isSuccess)
    }
}