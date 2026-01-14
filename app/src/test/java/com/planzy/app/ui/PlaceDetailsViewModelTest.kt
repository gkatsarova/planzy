package com.planzy.app.ui

import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.usecase.place.*
import com.planzy.app.ui.screens.place.PlaceDetailsViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PlaceDetailsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase = mockk()
    private val getPlaceReviewsUseCase: GetPlaceReviewsUseCase = mockk()
    private val getUserCommentsUseCase: GetUserCommentsUseCase = mockk()
    private val addUserCommentUseCase: AddUserCommentUseCase = mockk()
    private val updateUserCommentUseCase: UpdateUserCommentUseCase = mockk()
    private val deleteUserCommentUseCase: DeleteUserCommentUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private val locationId = "12345"
    private lateinit var viewModel: PlaceDetailsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getPlaceDetailsUseCase(any()) } returns Result.success(mockk(relaxed = true))
        coEvery { getPlaceReviewsUseCase(any(), any()) } returns Result.success(emptyList())
        coEvery { getUserCommentsUseCase(any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = PlaceDetailsViewModel(
            getPlaceDetailsUseCase,
            getPlaceReviewsUseCase,
            getUserCommentsUseCase,
            addUserCommentUseCase,
            updateUserCommentUseCase,
            deleteUserCommentUseCase,
            resourceProvider,
            locationId
        )
    }

    @Test
    fun `loadPlaceDetails success updates place state`() = runTest {
        val expectedPlace = mockk<Place>()
        coEvery { getPlaceDetailsUseCase(locationId) } returns Result.success(expectedPlace)

        createViewModel()

        assertEquals(expectedPlace, viewModel.place)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `loadPlaceDetails failure updates error message and place is null`() = runTest {
        val errorMsg = "Network Error"
        coEvery { getPlaceDetailsUseCase(locationId) } returns Result.failure(Exception(errorMsg))

        createViewModel()

        assertNull(viewModel.place)
        assertEquals(errorMsg, viewModel.errorMessage)
    }

    @Test
    fun `addUserComment success updates comments list`() = runTest {
        createViewModel()
        val newComment = mockk<UserComment>()
        coEvery { addUserCommentUseCase(locationId, "Super", 5) } returns Result.success(newComment)

        viewModel.addUserComment("Super", 5)

        assertFalse(viewModel.isSubmittingComment)
        assertTrue(viewModel.userComments.contains(newComment))
    }

    @Test
    fun `deleteUserComment success removes it from list`() = runTest {
        val commentId = "c1"
        val comment = mockk<UserComment> { every { id } returns commentId }
        coEvery { getUserCommentsUseCase(locationId) } returns Result.success(listOf(comment))

        createViewModel()

        coEvery { deleteUserCommentUseCase(commentId) } returns Result.success(Unit)

        viewModel.deleteUserComment(commentId)

        assertFalse(viewModel.userComments.any { it.id == commentId })
    }
}