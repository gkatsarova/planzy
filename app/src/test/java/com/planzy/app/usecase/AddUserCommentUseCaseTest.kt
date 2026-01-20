package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.AddUserCommentUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AddUserCommentUseCaseTest {

    private lateinit var repository: PlacesRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: AddUserCommentUseCase

    @Before
    fun setup() {
        repository = mockk()
        resourceProvider = mockk()
        useCase = AddUserCommentUseCase(repository, resourceProvider)

        every { resourceProvider.getString(R.string.empty_comment_text) } returns "Empty text"
        every { resourceProvider.getString(R.string.rating_error) } returns "Invalid rating"
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `valid comment returns success`() = runTest {
        val comment = mockk<UserComment>()
        coEvery { repository.addUserComment("id", "Cool place", 5) } returns Result.success(comment)

        val result = useCase("id", "Cool place", 5)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(comment, result.getOrNull())
    }

    @Test
    fun `blank text returns failure`() = runTest {
        val result = useCase("id", "  ", 5)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Empty text", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invalid rating returns failure`() = runTest {
        val result = useCase("id", "Cool place", 6)

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Invalid rating", result.exceptionOrNull()?.message)
    }
}