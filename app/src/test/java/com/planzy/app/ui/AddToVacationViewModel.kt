package com.planzy.app.ui

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationPlace
import com.planzy.app.domain.usecase.vacation.AddPlaceToVacationUseCase
import com.planzy.app.domain.usecase.vacation.CreateVacationUseCase
import com.planzy.app.domain.usecase.vacation.GetUserVacationsUseCase
import com.planzy.app.ui.screens.place.AddToVacationViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AddToVacationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val getUserVacationsUseCase: GetUserVacationsUseCase = mockk()
    private val createVacationUseCase: CreateVacationUseCase = mockk()
    private val addPlaceToVacationUseCase: AddPlaceToVacationUseCase = mockk()
    private val resourceProvider: ResourceProvider = mockk()

    private lateinit var viewModel: AddToVacationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getUserVacationsUseCase() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = AddToVacationViewModel(
            getUserVacationsUseCase,
            createVacationUseCase,
            addPlaceToVacationUseCase,
            resourceProvider
        )
    }

    @Test
    fun `initialization loads vacations`() = runTest {
        val expectedVacations = listOf(
            mockk<Vacation>(relaxed = true),
            mockk<Vacation>(relaxed = true)
        )
        coEvery { getUserVacationsUseCase() } returns Result.success(expectedVacations)

        createViewModel()

        assertEquals(expectedVacations, viewModel.vacations)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `loadVacations failure updates error message`() = runTest {
        val errorMsg = "Failed to load"
        coEvery { getUserVacationsUseCase() } returns Result.failure(Exception(errorMsg))

        createViewModel()

        assertTrue(viewModel.vacations.isEmpty())
        assertEquals(errorMsg, viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `createVacation success adds vacation to list and calls onSuccess`() = runTest {
        createViewModel()

        val title = "Summer Trip"
        val newVacation = mockk<Vacation>(relaxed = true)
        val onSuccessCallback = mockk<(Vacation) -> Unit>(relaxed = true)

        coEvery { createVacationUseCase(title) } returns Result.success(newVacation)

        viewModel.createVacation(title, onSuccessCallback)

        assertTrue(viewModel.vacations.contains(newVacation))
        assertFalse(viewModel.isCreatingVacation)
        assertNull(viewModel.errorMessage)
        verify { onSuccessCallback(newVacation) }
    }

    @Test
    fun `createVacation failure updates error message and does not call onSuccess`() = runTest {
        createViewModel()

        val title = "Summer Trip"
        val errorMsg = "Creation failed"
        val onSuccessCallback = mockk<(Vacation) -> Unit>()

        coEvery { createVacationUseCase(title) } returns Result.failure(Exception(errorMsg))

        viewModel.createVacation(title, onSuccessCallback)

        assertEquals(errorMsg, viewModel.errorMessage)
        assertFalse(viewModel.isCreatingVacation)
        verify(exactly = 0) { onSuccessCallback(any()) }
    }

    @Test
    fun `addPlaceToVacation success shows success message and reloads vacations`() = runTest {
        val successMsg = "Place added successfully"
        every { resourceProvider.getString(R.string.place_added_to_vacation) } returns successMsg

        val initialVacations = listOf(mockk<Vacation>(relaxed = true))
        val reloadedVacations = listOf(mockk<Vacation>(relaxed = true), mockk<Vacation>(relaxed = true))

        coEvery { getUserVacationsUseCase() } returnsMany listOf(
            Result.success(initialVacations),
            Result.success(reloadedVacations)
        )

        createViewModel()

        val vacationId = "vacation123"
        val placeId = "place456"
        val onSuccessCallback = mockk<() -> Unit>(relaxed = true)
        val mockVacationPlace = mockk<VacationPlace>(relaxed = true)

        coEvery { addPlaceToVacationUseCase(vacationId, placeId) } returns Result.success(mockVacationPlace)

        viewModel.addPlaceToVacation(vacationId, placeId, onSuccessCallback)

        assertEquals(successMsg, viewModel.successMessage)
        assertEquals(reloadedVacations, viewModel.vacations)
        assertFalse(viewModel.isAddingPlace)
        assertNull(viewModel.errorMessage)
        verify { onSuccessCallback() }
    }

    @Test
    fun `addPlaceToVacation failure updates error message and does not call onSuccess`() = runTest {
        createViewModel()

        val vacationId = "vacation123"
        val placeId = "place456"
        val errorMsg = "Failed to add place"
        val onSuccessCallback = mockk<() -> Unit>()

        coEvery { addPlaceToVacationUseCase(vacationId, placeId) } returns Result.failure(Exception(errorMsg))

        viewModel.addPlaceToVacation(vacationId, placeId, onSuccessCallback)

        assertEquals(errorMsg, viewModel.errorMessage)
        assertFalse(viewModel.isAddingPlace)
        assertNull(viewModel.successMessage)
        verify(exactly = 0) { onSuccessCallback() }
    }

    @Test
    fun `clearMessages clears both error and success messages`() = runTest {
        every { resourceProvider.getString(R.string.place_added_to_vacation) } returns "Success"

        val vacationId = "vacation123"
        val placeId = "place456"
        val onSuccessCallback = mockk<() -> Unit>(relaxed = true)
        val mockVacationPlace = mockk<VacationPlace>(relaxed = true)

        coEvery { addPlaceToVacationUseCase(vacationId, placeId) } returns Result.success(mockVacationPlace)
        coEvery { getUserVacationsUseCase() } returns Result.success(emptyList())

        createViewModel()

        viewModel.addPlaceToVacation(vacationId, placeId, onSuccessCallback)

        assertNotNull(viewModel.successMessage)

        viewModel.clearMessages()

        assertNull(viewModel.errorMessage)
        assertNull(viewModel.successMessage)
    }

    @Test
    fun `loadVacations can be called manually to refresh list`() = runTest {
        val initialVacations = listOf(mockk<Vacation>(relaxed = true))
        val updatedVacations = listOf(
            mockk<Vacation>(relaxed = true),
            mockk<Vacation>(relaxed = true)
        )

        coEvery { getUserVacationsUseCase() } returnsMany listOf(
            Result.success(initialVacations),
            Result.success(updatedVacations)
        )

        createViewModel()

        assertEquals(1, viewModel.vacations.size)

        viewModel.loadVacations()

        assertEquals(2, viewModel.vacations.size)
        assertEquals(updatedVacations, viewModel.vacations)
    }

    @Test
    fun `createVacation adds new vacation at the beginning of list`() = runTest {
        val existingVacation = mockk<Vacation>(relaxed = true) {
            every { id } returns "existing"
        }
        val newVacation = mockk<Vacation>(relaxed = true) {
            every { id } returns "new"
        }

        coEvery { getUserVacationsUseCase() } returns Result.success(listOf(existingVacation))
        coEvery { createVacationUseCase(any()) } returns Result.success(newVacation)

        createViewModel()

        assertEquals(1, viewModel.vacations.size)

        viewModel.createVacation("New Trip") {}

        assertEquals(2, viewModel.vacations.size)
        assertEquals("new", viewModel.vacations[0].id)
        assertEquals("existing", viewModel.vacations[1].id)
    }
}