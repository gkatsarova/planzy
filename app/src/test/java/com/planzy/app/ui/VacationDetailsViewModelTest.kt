package com.planzy.app.ui

import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.auth.GetCurrentUserUseCase
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

    private val getVacationDetailsUseCase: GetVacationDetailsUseCase = mockk()
    private val removePlaceFromVacationUseCase: RemovePlaceFromVacationUseCase = mockk()
    private val getVacationCommentsUseCase: GetVacationCommentsUseCase = mockk()
    private val addVacationCommentUseCase: AddVacationCommentUseCase = mockk()
    private val updateVacationCommentUseCase: UpdateVacationCommentUseCase = mockk()
    private val deleteVacationCommentUseCase: DeleteVacationCommentUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val placesRepository: PlacesRepository = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private val vacationId = "vacation123"
    private lateinit var viewModel: VacationDetailsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getCurrentUserUseCase() } returns null
        coEvery { getVacationDetailsUseCase(any()) } returns Result.success(
            VacationDetails(
                vacation = mockk(relaxed = true),
                creatorUsername = "testUser",
                places = emptyList()
            )
        )
        coEvery { getVacationCommentsUseCase(any()) } returns Result.success(emptyList())
        coEvery { placesRepository.getUserCommentsStats(any()) } returns Result.success(Pair(null, 0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = VacationDetailsViewModel(
            getVacationDetailsUseCase,
            removePlaceFromVacationUseCase,
            getVacationCommentsUseCase,
            addVacationCommentUseCase,
            updateVacationCommentUseCase,
            deleteVacationCommentUseCase,
            getCurrentUserUseCase,
            placesRepository,
            resourceProvider,
            vacationId
        )
    }

    @Test
    fun `loadVacationDetails success updates vacation state`() = runTest {
        val expectedVacation = mockk<Vacation>(relaxed = true)
        val expectedDetails = VacationDetails(expectedVacation, "testUser", emptyList())
        coEvery { getVacationDetailsUseCase(vacationId) } returns Result.success(expectedDetails)

        createViewModel()

        assertEquals(expectedVacation, viewModel.vacation)
        assertEquals("testUser", viewModel.creatorUsername)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `loadVacationDetails failure updates error message and vacation is null`() = runTest {
        val errorMsg = "Network Error"
        coEvery { getVacationDetailsUseCase(vacationId) } returns Result.failure(Exception(errorMsg))

        createViewModel()

        assertNull(viewModel.vacation)
        assertEquals(errorMsg, viewModel.errorMessage)
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

        val mockDetails = VacationDetails(mockVacation, "testUser", listOf(mockPlace))

        coEvery { getVacationDetailsUseCase(vacationId) } returns Result.success(mockDetails)
        coEvery { removePlaceFromVacationUseCase(vacationId, placeId) } returns Result.success(Unit)

        createViewModel()

        viewModel.removePlaceFromVacation(placeId)

        assertFalse(viewModel.places.any { it.id == placeId })
        assertEquals(1, viewModel.vacation?.placesCount)
    }

    @Test
    fun `removePlaceFromVacation failure updates error message`() = runTest {
        val errorMsg = "Delete failed"
        val placeId = "place1"

        createViewModel()

        coEvery { removePlaceFromVacationUseCase(vacationId, placeId) } returns Result.failure(Exception(errorMsg))

        viewModel.removePlaceFromVacation(placeId)

        assertEquals(errorMsg, viewModel.errorMessage)
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
        createViewModel()

        val errorMsg = "Failed to add"
        coEvery { addVacationCommentUseCase(vacationId, any()) } returns Result.failure(Exception(errorMsg))

        viewModel.addVacationComment("Test")

        assertFalse(viewModel.isSubmittingComment)
        assertEquals(errorMsg, viewModel.commentErrorMessage)
    }

    @Test
    fun `updateVacationComment success reloads comments`() = runTest {
        val commentId = "c1"
        val updatedComment = mockk<VacationComment>(relaxed = true)

        coEvery { getVacationCommentsUseCase(vacationId) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(updatedComment))
        )
        coEvery { updateVacationCommentUseCase(commentId, "Updated") } returns Result.success(Unit)

        createViewModel()

        viewModel.updateVacationComment(commentId, "Updated")

        assertFalse(viewModel.isUpdatingComment)
        assertTrue(viewModel.vacationComments.contains(updatedComment))
    }

    @Test
    fun `updateVacationComment failure updates error message`() = runTest {
        createViewModel()

        val errorMsg = "Update failed"
        coEvery { updateVacationCommentUseCase(any(), any()) } returns Result.failure(Exception(errorMsg))

        viewModel.updateVacationComment("c1", "Updated")

        assertFalse(viewModel.isUpdatingComment)
        assertEquals(errorMsg, viewModel.commentErrorMessage)
    }

    @Test
    fun `deleteVacationComment success removes it from list`() = runTest {
        val commentId = "c1"
        val comment = mockk<VacationComment>(relaxed = true) {
            every { id } returns commentId
        }
        coEvery { getVacationCommentsUseCase(vacationId) } returns Result.success(listOf(comment))

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

        coVerify(exactly = 2) { getVacationDetailsUseCase(vacationId) }
        coVerify(exactly = 2) { getVacationCommentsUseCase(vacationId) }
    }
}