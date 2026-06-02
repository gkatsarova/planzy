package com.planzy.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.planzy.app.R
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.place.GetUserCommentsStatsUseCase
import com.planzy.app.domain.model.SearchAllOutcome
import com.planzy.app.domain.model.SearchAllParams
import com.planzy.app.domain.model.SearchAllResult
import com.planzy.app.domain.usecase.search.SearchAllUseCase
import com.planzy.app.domain.usecase.vacation.GetVacationCommentsCountUseCase
import com.planzy.app.ui.screens.SearchViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAllUseCase: SearchAllUseCase
    private lateinit var getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase
    private lateinit var getVacationCommentsCountUseCase: GetVacationCommentsCountUseCase
    private lateinit var entityExtractor: LocationEntityExtractor
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        searchAllUseCase = mockk()
        getUserCommentsStatsUseCase = mockk()
        getVacationCommentsCountUseCase = mockk()
        entityExtractor = mockk()
        resourceProvider = mockk(relaxed = true)
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk()

        every { context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.getBoolean("perm_granted", false) } returns false
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        coEvery { entityExtractor.initialize() } just runs

        viewModel = SearchViewModel(
            searchAllUseCase = searchAllUseCase,
            getUserCommentsStatsUseCase = getUserCommentsStatsUseCase,
            getVacationCommentsCountUseCase = getVacationCommentsCountUseCase,
            entityExtractor = entityExtractor,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initialization sets correct default values`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.isLoading)
        assertTrue(viewModel.places.isEmpty())
        assertTrue(viewModel.vacations.isEmpty())
        assertTrue(viewModel.users.isEmpty())
        assertTrue(viewModel.showLocationDialog)
    }

    @Test
    fun `search with blank query resets all state without calling use case`() = runTest {
        viewModel.search("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { searchAllUseCase(any()) }
        assertTrue(viewModel.places.isEmpty())
        assertTrue(viewModel.vacations.isEmpty())
        assertTrue(viewModel.users.isEmpty())
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `successful search populates places, vacations, and users`() = runTest {
        val vacation = Vacation(
            id = "v1",
            userId = "u1",
            title = "Paris Trip",
            createdAt = "2025-01-01",
            placesCount = 2,
            commentsCount = 0
        )

        coEvery { searchAllUseCase(any()) } returns SearchAllOutcome.Success(
            SearchAllResult(vacations = listOf(vacation))
        )
        coEvery { getVacationCommentsCountUseCase("v1") } returns Result.success(3)
        coEvery { getUserCommentsStatsUseCase(any()) } returns Result.success(Pair(null, 0))

        viewModel.search("Paris")
        advanceUntilIdle()

        assertEquals(1, viewModel.vacations.size)
        assertEquals(3, viewModel.vacations[0].commentsCount)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `empty outcome sets error message and clears results`() = runTest {
        every { resourceProvider.getString(R.string.error_no_results_found) } returns "No results"

        coEvery { searchAllUseCase(any()) } returns SearchAllOutcome.Empty("No results")

        viewModel.search("Nowhere")
        advanceUntilIdle()

        assertEquals("No results", viewModel.errorMessage)
        assertTrue(viewModel.places.isEmpty())
        assertTrue(viewModel.vacations.isEmpty())
        assertTrue(viewModel.users.isEmpty())
    }

    @Test
    fun `partial result outcome still populates available data`() = runTest {
        val vacation = Vacation(
            id = "v1",
            userId = "u1",
            title = "Rome Trip",
            createdAt = "2025-01-01",
            placesCount = 1,
            commentsCount = 0
        )

        coEvery { searchAllUseCase(any()) } returns SearchAllOutcome.PlacesError(
            message = "API limit",
            partialResult = SearchAllResult(vacations = listOf(vacation))
        )
        coEvery { getVacationCommentsCountUseCase("v1") } returns Result.success(0)

        viewModel.search("Rome")
        advanceUntilIdle()

        assertEquals(1, viewModel.vacations.size)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `search passes correct location params when permission granted and location set`() = runTest {
        viewModel.setLocationPermission(true)
        viewModel.setUserLocation(48.8, 2.3)

        coEvery { searchAllUseCase(any()) } returns SearchAllOutcome.Empty("No results")

        viewModel.search("coffee")
        advanceUntilIdle()

        coVerify {
            searchAllUseCase(
                SearchAllParams(
                    query = "coffee",
                    userLocation = Pair(48.8, 2.3),
                    locationPermissionGranted = true
                )
            )
        }
    }

    @Test
    fun `search without location permission passes null location`() = runTest {
        coEvery { searchAllUseCase(any()) } returns SearchAllOutcome.Empty("No results")

        viewModel.search("pizza")
        advanceUntilIdle()

        coVerify {
            searchAllUseCase(
                SearchAllParams(
                    query = "pizza",
                    userLocation = null,
                    locationPermissionGranted = false
                )
            )
        }
    }

    @Test
    fun `clearSearch resets all state`() = runTest {
        coEvery { searchAllUseCase(any()) } returns SearchAllOutcome.Success(
            SearchAllResult(
                vacations = listOf(
                    Vacation("v1", "u1", "Trip", "2025-01-01", 1, 0)
                )
            )
        )
        coEvery { getVacationCommentsCountUseCase(any()) } returns Result.success(0)
        viewModel.search("something")
        advanceUntilIdle()

        viewModel.clearSearch()

        assertEquals("", viewModel.searchQuery)
        assertTrue(viewModel.places.isEmpty())
        assertTrue(viewModel.placesWithStats.isEmpty())
        assertTrue(viewModel.vacations.isEmpty())
        assertTrue(viewModel.users.isEmpty())
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isSearchBarFocused)
    }

    @Test
    fun `setLocationPermission true saves preference and hides dialog`() {
        viewModel.setLocationPermission(true)

        assertTrue(viewModel.locationPermissionGranted)
        assertFalse(viewModel.showLocationDialog)
        verify { editor.putBoolean("perm_granted", true) }
        verify { editor.apply() }
    }

    @Test
    fun `dismissLocationDialog hides dialog without changing permission`() {
        viewModel.dismissLocationDialog()

        assertFalse(viewModel.showLocationDialog)
        assertFalse(viewModel.locationPermissionGranted)
    }


    @Test
    fun `updateSearchBarFocus updates state`() {
        viewModel.updateSearchBarFocus(true)
        assertTrue(viewModel.isSearchBarFocused)

        viewModel.updateSearchBarFocus(false)
        assertFalse(viewModel.isSearchBarFocused)
    }
}