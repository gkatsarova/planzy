package com.planzy.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.planzy.app.R
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.usecase.place.SearchPlacesUseCase
import com.planzy.app.domain.usecase.place.GetUserCommentsStatsUseCase
import com.planzy.app.domain.usecase.vacation.SearchVacationsUseCase
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
    private lateinit var searchPlacesUseCase: SearchPlacesUseCase
    private lateinit var searchVacationsUseCase: SearchVacationsUseCase
    private lateinit var getVacationCommentsCountUseCase: GetVacationCommentsCountUseCase
    private lateinit var entityExtractor: LocationEntityExtractor
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var getUserCommentsStatsUseCase: GetUserCommentsStatsUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        searchPlacesUseCase = mockk()
        searchVacationsUseCase = mockk()
        getVacationCommentsCountUseCase = mockk()
        entityExtractor = mockk()
        resourceProvider = mockk(relaxed = true)
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk()
        getUserCommentsStatsUseCase = mockk()

        every { context.getSharedPreferences("planzy_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.getBoolean("perm_granted", false) } returns false
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        coEvery { entityExtractor.initialize() } just runs
        coEvery { entityExtractor.extractLocation(any()) } returns null

        coEvery { searchVacationsUseCase(any()) } returns Result.success(emptyList())

        viewModel = SearchViewModel(
            searchPlacesUseCase,
            getUserCommentsStatsUseCase,
            searchVacationsUseCase,
            getVacationCommentsCountUseCase,
            entityExtractor,
            resourceProvider,
            context
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
        assertTrue(viewModel.showLocationDialog)
    }

    @Test
    fun `searchForPlaces with blank query does nothing`() = runTest {
        viewModel.searchForPlaces("   ")
        advanceUntilIdle()

        assertTrue(viewModel.places.isEmpty())
        assertTrue(viewModel.vacations.isEmpty())
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `searchForPlaces shows API error when places fail but vacations succeed`() = runTest {
        every { resourceProvider.getString(R.string.error_api_limit) } returns "Limit Error"

        val mockVacation = Vacation(
            id = "vacation1",
            userId = "user1",
            title = "Paris Trip",
            createdAt = "2025-01-01",
            placesCount = 3,
            commentsCount = 0
        )

        coEvery {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.failure(Exception("429"))

        coEvery { searchVacationsUseCase("Paris") } returns Result.success(listOf(mockVacation))
        coEvery { getVacationCommentsCountUseCase("vacation1") } returns Result.success(0)

        viewModel.searchForPlaces("Paris")
        advanceUntilIdle()

        assertEquals("Limit Error", viewModel.errorMessage)
        assertTrue(viewModel.places.isEmpty())
        assertEquals(1, viewModel.vacations.size)
    }

    @Test
    fun `searchForPlaces shows no results message when both fail with empty results`() = runTest {
        every { resourceProvider.getString(R.string.error_no_results_found) } returns "No results found"

        coEvery {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.success(emptyList())

        coEvery { searchVacationsUseCase("Paris") } returns Result.success(emptyList())

        viewModel.searchForPlaces("Paris")
        advanceUntilIdle()

        assertEquals("No results found", viewModel.errorMessage)
        assertTrue(viewModel.places.isEmpty())
        assertTrue(viewModel.vacations.isEmpty())
    }

    @Test
    fun `searchForPlaces handles empty results message`() = runTest {
        every { resourceProvider.getString(R.string.error_no_results_found) } returns "No results"
        coEvery {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.success(emptyList())
        coEvery { searchVacationsUseCase(any()) } returns Result.success(emptyList())

        viewModel.searchForPlaces("EmptyPlace")
        advanceUntilIdle()

        assertEquals("No results", viewModel.errorMessage)
    }

    @Test
    fun `searchForPlaces successfully loads places and vacations with comments`() = runTest {
        val mockVacation = Vacation(
            id = "vacation1",
            userId = "user1",
            title = "Summer Trip",
            createdAt = "2025-01-01",
            placesCount = 3,
            commentsCount = 0
        )

        coEvery {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.success(emptyList())
        coEvery { searchVacationsUseCase("Paris") } returns Result.success(listOf(mockVacation))
        coEvery { getVacationCommentsCountUseCase("vacation1") } returns Result.success(5)
        coEvery { getUserCommentsStatsUseCase(any()) } returns Result.success(Pair(null, 0))

        viewModel.searchForPlaces("Paris")
        advanceUntilIdle()

        assertEquals(1, viewModel.vacations.size)
        assertEquals(5, viewModel.vacations[0].commentsCount)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `searchForPlaces uses GPS when no city is detected in query and permission granted`() = runTest {
        viewModel.setLocationPermission(true)
        viewModel.setUserLocation(42.0, 23.0)

        coEvery { entityExtractor.extractLocation("bar") } returns null
        coEvery {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.success(emptyList())
        coEvery { getUserCommentsStatsUseCase(any()) } returns Result.success(Pair(null, 0))

        viewModel.searchForPlaces("bar")
        advanceUntilIdle()

        coVerify {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = "42.0,23.0",
                radius = 25
            )
        }
    }

    @Test
    fun `searchForPlaces bypasses GPS when city is detected in query`() = runTest {
        viewModel.setLocationPermission(true)
        viewModel.setUserLocation(42.0, 23.0)

        val mockAnnotation = mockk<com.google.mlkit.nl.entityextraction.EntityAnnotation>()
        val mockEntity = mockk<com.google.mlkit.nl.entityextraction.Entity>()
        every { mockEntity.type } returns com.google.mlkit.nl.entityextraction.Entity.TYPE_ADDRESS
        every { mockAnnotation.entities } returns listOf(mockEntity)

        coEvery { entityExtractor.extractLocation("Sofia") } returns mockAnnotation
        coEvery {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.success(emptyList())
        coEvery { getUserCommentsStatsUseCase(any()) } returns Result.success(Pair(null, 0))

        viewModel.searchForPlaces("bars in Sofia")
        advanceUntilIdle()

        coVerify {
            searchPlacesUseCase(
                destination = any(),
                minRating = any(),
                maxResults = any(),
                latLong = null,
                radius = null
            )
        }
    }

    @Test
    fun `setLocationPermission updates preferences and hides dialog`() {
        viewModel.setLocationPermission(true)

        assertTrue(viewModel.locationPermissionGranted)
        assertFalse(viewModel.showLocationDialog)
        verify { editor.putBoolean("perm_granted", true) }
        verify { editor.apply() }
    }

    @Test
    fun `dismissLocationDialog hides dialog without changing permissions`() {
        viewModel.dismissLocationDialog()
        assertFalse(viewModel.showLocationDialog)
    }


    @Test
    fun `updateSearchBarFocus updates focus state`() {
        viewModel.updateSearchBarFocus(true)
        assertTrue(viewModel.isSearchBarFocused)

        viewModel.updateSearchBarFocus(false)
        assertFalse(viewModel.isSearchBarFocused)
    }
}