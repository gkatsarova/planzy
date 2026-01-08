package com.planzy.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.planzy.app.R
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.usecase.api.SearchPlacesUseCase
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
    private lateinit var entityExtractor: LocationEntityExtractor
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        searchPlacesUseCase = mockk()
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
        coEvery { entityExtractor.extractLocation(any()) } returns null

        viewModel = SearchViewModel(
            searchPlacesUseCase,
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
        assertTrue(viewModel.showLocationDialog)
    }

    @Test
    fun `searchForPlaces with blank query does nothing`() = runTest {
        viewModel.searchForPlaces("   ")
        advanceUntilIdle()
        coVerify(exactly = 0) { searchPlacesUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `searchForPlaces handles API 429 error correctly`() = runTest {
        every { resourceProvider.getString(R.string.error_api_limit) } returns "Limit Error"
        coEvery { searchPlacesUseCase(any(), any(), any(), any(), any()) } returns Result.failure(Exception("API_ERROR_429"))

        viewModel.searchForPlaces("Paris")
        advanceUntilIdle()

        assertEquals("Limit Error", viewModel.errorMessage)
        assertTrue(viewModel.places.isEmpty())
    }

    @Test
    fun `searchForPlaces handles empty results message`() = runTest {
        every { resourceProvider.getString(R.string.error_no_places_found) } returns "No results"
        coEvery { searchPlacesUseCase(any(), any(), any(), any(), any()) } returns Result.success(emptyList())

        viewModel.searchForPlaces("EmptyPlace")
        advanceUntilIdle()

        assertEquals("No results", viewModel.errorMessage)
    }

    @Test
    fun `searchForPlaces uses GPS when no city is detected in query and permission granted`() = runTest {
        viewModel.setLocationPermission(true)
        viewModel.setUserLocation(42.0, 23.0)

        coEvery { entityExtractor.extractLocation("bar") } returns null
        coEvery { searchPlacesUseCase(any(), any(), any(), any(), any()) } returns Result.success(emptyList())

        viewModel.searchForPlaces("bar")
        advanceUntilIdle()

        coVerify { searchPlacesUseCase(any(), any(), any(), eq("42.0,23.0"), eq(25)) }
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
        coEvery { searchPlacesUseCase(any(), any(), any(), any(), any()) } returns Result.success(emptyList())

        viewModel.searchForPlaces("bars in Sofia")
        advanceUntilIdle()

        coVerify { searchPlacesUseCase(any(), any(), any(), isNull(), isNull()) }
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
}