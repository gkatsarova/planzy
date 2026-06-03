package com.planzy.app.ui

import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceDetailsData
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

    private val getPlaceDataUseCase: GetPlaceDataUseCase = mockk()
    private val addUserCommentUseCase: AddUserCommentUseCase = mockk()
    private val updateUserCommentUseCase: UpdateUserCommentUseCase = mockk()
    private val deleteUserCommentUseCase: DeleteUserCommentUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private val locationId = "12345"
    private lateinit var viewModel: PlaceDetailsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val defaultMockData = PlaceDetailsData(
            place = mockk(relaxed = true),
            reviews = emptyList(),
            userComments = emptyList()
        )
        coEvery { getPlaceDataUseCase(any(), any()) } returns Result.success(defaultMockData)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = PlaceDetailsViewModel(
            getPlaceDataUseCase = getPlaceDataUseCase,
            addUserCommentUseCase = addUserCommentUseCase,
            updateUserCommentUseCase = updateUserCommentUseCase,
            deleteUserCommentUseCase = deleteUserCommentUseCase,
            resourceProvider = resourceProvider,
            locationId = locationId
        )
    }

    @Test
    fun `loadPlaceDetails success updates place state`() = runTest {
        val expectedPlace = mockk<Place>()
        val successData = PlaceDetailsData(
            place = expectedPlace,
            reviews = emptyList(),
            userComments = emptyList()
        )
        coEvery { getPlaceDataUseCase(locationId, any()) } returns Result.success(successData)

        createViewModel()

        assertEquals(expectedPlace, viewModel.place)
        assertNull(viewModel.errorMessage)
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

        val initialData = PlaceDetailsData(
            place = mockk(relaxed = true),
            reviews = emptyList(),
            userComments = listOf(comment)
        )
        coEvery { getPlaceDataUseCase(locationId, any()) } returns Result.success(initialData)

        createViewModel()

        coEvery { deleteUserCommentUseCase(commentId) } returns Result.success(Unit)

        viewModel.deleteUserComment(commentId)

        assertFalse(viewModel.userComments.any { it.id == commentId })
    }
}