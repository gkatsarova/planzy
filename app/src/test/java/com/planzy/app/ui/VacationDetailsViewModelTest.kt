package com.planzy.app.ui

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.model.VacationDetails
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
import com.planzy.app.domain.usecase.place.GetUserCommentsStatsUseCase
import com.planzy.app.ui.screens.vacation.VacationDetailsViewModel
import com.planzy.app.domain.usecase.vacation.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class VacationDetailsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val getVacationDataUseCase: GetVacationDataUseCase = mockk()
    private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase = mockk()
    private val addVacationCommentUseCase: AddVacationCommentUseCase = mockk()
    private val updateVacationCommentUseCase: UpdateVacationCommentUseCase = mockk()
    private val deleteVacationCommentUseCase: DeleteVacationCommentUseCase = mockk()
    private val manageSavedVacationUseCase: ManageSavedVacationUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase = mockk()
    private val isVacationSavedUseCase: IsVacationSavedUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private val vacationId = "vacation123"
    private lateinit var viewModel: VacationDetailsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getCurrentUserUseCase() } returns null

        coEvery { getVacationDataUseCase(any()) } returns Result.success(
            VacationDetails(
                vacation = mockk(relaxed = true),
                creatorUsername = "testUser",
                places = emptyList(),
                vacationComments = emptyList()
            )
        )
        coEvery { getUserCommentsStatsUseCase(any()) } returns Result.success(Pair(null, 0))
        coEvery { isVacationSavedUseCase(any()) } returns Result.success(false)

        every { resourceProvider.getString(R.string.unknown_error) } returns "Unknown Error"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun createViewModel() {
        viewModel = VacationDetailsViewModel(
            getVacationDataUseCase = getVacationDataUseCase,
            removePlaceFromVacationUseCase = removePlaceFromVacationUseCase,
            addVacationCommentUseCase = addVacationCommentUseCase,
            updateVacationCommentUseCase = updateVacationCommentUseCase,
            deleteVacationCommentUseCase = deleteVacationCommentUseCase,
            manageSavedVacationUseCase = manageSavedVacationUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            getUserCommentsStatsUseCase = getUserCommentsStatsUseCase,
            isVacationSavedUseCase = isVacationSavedUseCase,
            resourceProvider = resourceProvider,
            vacationId = vacationId
        )
    }

    @Test
    fun `loadVacationDetails success updates vacation state`() = runTest {
        val expectedVacation = mockk<Vacation>(relaxed = true)
        val expectedComments = listOf(mockk<VacationComment>(relaxed = true))
        val expectedDetails = VacationDetails(expectedVacation, "testUser", emptyList(), expectedComments)

        coEvery { getVacationDataUseCase(vacationId) } returns Result.success(expectedDetails)

        createViewModel()

        assertEquals(expectedVacation, viewModel.vacation)
        assertEquals("testUser", viewModel.creatorUsername)
        assertEquals(expectedComments, viewModel.vacationComments)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `removePlaceFromVacation success updates places list and count`() = runTest {
        val placeId = "place1"
        val mockPlace = mockk<Place>(relaxed = true) {
            every { id } returns placeId
        }
        val mockVacation = mockk<Vacation>(relaxed = true) {
            every { placesCount } returns 2
        }
        val updatedVacation = mockk<Vacation>(relaxed = true) {
            every { placesCount } returns 1
        }
        every { mockVacation.copy(placesCount = 1) } returns updatedVacation

        val mockDetails = VacationDetails(mockVacation, "testUser", listOf(mockPlace), emptyList())

        coEvery { getVacationDataUseCase(vacationId) } returns Result.success(mockDetails)
        coEvery { removePlaceFromVacationUseCase(vacationId, placeId) } returns Result.success(Unit)

        createViewModel()

        viewModel.removePlaceFromVacation(placeId)

        assertFalse(viewModel.places.any { it.id == placeId })
        assertEquals(1, viewModel.vacation?.placesCount)
    }

    @Test
    fun `removePlaceFromVacation failure updates error message`() = runTest {
        val expectedMessage = "Error removing place"
        val placeId = "place1"
        every { resourceProvider.getString(R.string.error_removing_place) } returns expectedMessage

        createViewModel()

        coEvery { removePlaceFromVacationUseCase(vacationId, placeId) } returns Result.failure(AppException(AppError.ERROR_REMOVING_PLACE))

        viewModel.removePlaceFromVacation(placeId)

        assertEquals(expectedMessage, viewModel.errorMessage)
    }

    @Test
    fun `addVacationComment success updates comments list`() = runTest {
        createViewModel()

        val newComment = mockk<VacationComment>(relaxed = true)
        coEvery { addVacationCommentUseCase(vacationId, "Great!") } returns Result.success(newComment)

        viewModel.addVacationComment("Great!")

        assertFalse(viewModel.isSubmittingComment)
        assertTrue(viewModel.vacationComments.contains(newComment))
        assertNull(viewModel.commentErrorMessage)
    }

    @Test
    fun `addVacationComment failure updates error message`() = runTest {
        val expectedMessage = "Error posting comment"
        every { resourceProvider.getString(R.string.error_posting_comment) } returns expectedMessage

        createViewModel()

        coEvery { addVacationCommentUseCase(vacationId, any()) } returns Result.failure(AppException(AppError.ERROR_POSTING_COMMENT))

        viewModel.addVacationComment("Test")

        assertFalse(viewModel.isSubmittingComment)
        assertEquals(expectedMessage, viewModel.commentErrorMessage)
    }

    @Test
    fun `updateVacationComment success reloads data`() = runTest {
        val commentId = "c1"
        val updatedComment = mockk<VacationComment>(relaxed = true)
        val detailsWithUpdatedComment = VacationDetails(mockk(relaxed = true), "testUser", emptyList(), listOf(updatedComment))

        coEvery { getVacationDataUseCase(vacationId) } returnsMany listOf(
            Result.success(VacationDetails(mockk(relaxed = true), "testUser", emptyList(), emptyList())),
            Result.success(detailsWithUpdatedComment)
        )
        coEvery { updateVacationCommentUseCase(commentId, "Updated") } returns Result.success(Unit)

        createViewModel()

        viewModel.updateVacationComment(commentId, "Updated")

        assertFalse(viewModel.isUpdatingComment)
        assertTrue(viewModel.vacationComments.contains(updatedComment))
    }

    @Test
    fun `updateVacationComment failure updates error message`() = runTest {
        val expectedMessage = "Error updating comment"
        every { resourceProvider.getString(R.string.error_updating_comment) } returns expectedMessage

        createViewModel()

        coEvery { updateVacationCommentUseCase(any(), any()) } returns Result.failure(AppException(AppError.ERROR_UPDATING_COMMENT))

        viewModel.updateVacationComment("c1", "Updated")

        assertFalse(viewModel.isUpdatingComment)
        assertEquals(expectedMessage, viewModel.commentErrorMessage)
    }

    @Test
    fun `deleteVacationComment success removes it from list`() = runTest {
        val commentId = "c1"
        val comment = mockk<VacationComment>(relaxed = true) {
            every { id } returns commentId
        }
        val details = VacationDetails(mockk(relaxed = true), "testUser", emptyList(), listOf(comment))
        coEvery { getVacationDataUseCase(vacationId) } returns Result.success(details)

        createViewModel()

        coEvery { deleteVacationCommentUseCase(commentId) } returns Result.success(Unit)

        viewModel.deleteVacationComment(commentId)

        assertFalse(viewModel.vacationComments.any { it.id == commentId })
        assertFalse(viewModel.isDeletingComment)
    }

    @Test
    fun `onRetry reloads all data`() = runTest {
        createViewModel()

        viewModel.onRetry()

        coVerify(exactly = 2) { getVacationDataUseCase(vacationId) }
    }
}