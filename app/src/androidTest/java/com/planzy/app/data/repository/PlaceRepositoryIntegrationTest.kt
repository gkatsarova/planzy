package com.planzy.app.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.TripadvisorApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceRepositoryIntegrationTest : BaseRepositoryIntegrationTest() {

    private lateinit var placesRepository: PlacesRepositoryImpl

    companion object {
        const val KNOWN_LOCATION_ID = "188151" // Eiffel Tower
    }

    @Before
    fun setup() {
        placesRepository = PlacesRepositoryImpl(
            tripadvisorApi = TripadvisorApi(),
            supabaseClient = SupabaseClient,
            resourceProvider = getResourceProvider()
        )
    }

    @Test
    fun searchPlacesWithValidQueryReturnsNonEmptyList() = runTest {
        val result = placesRepository.searchPlaces("hotel", 0.0, null, null)
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun getPlaceDetailsWithKnownIdReturnsCorrectPlace() = runTest {
        val result = placesRepository.getPlaceDetails(KNOWN_LOCATION_ID)
        assertTrue(result.isSuccess)
        val place = result.getOrNull()
        assertNotNull(place)
        assertTrue(place!!.name.contains("Eiffel"))
    }

    @Test
    fun getPlaceDetailsWithKnownIdReturnsNonEmptyDescription() = runTest {
        val result = placesRepository.getPlaceDetails(KNOWN_LOCATION_ID)
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()?.description.isNullOrEmpty())
    }

    @Test
    fun getPlacePhotosWithKnownIdReturnsSuccessResult() = runTest {
        val result = placesRepository.getPlacePhotos(KNOWN_LOCATION_ID)
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun getPlacePhotosReturnsValidHttpUrls() = runTest {
        val result = placesRepository.getPlacePhotos(KNOWN_LOCATION_ID)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach { url ->
            assertTrue(url.startsWith("http"))
        }
    }

    @Test
    fun getPlaceReviewsWithKnownIdReturnsSuccessResult() = runTest {
        val result = placesRepository.getPlaceReviews(KNOWN_LOCATION_ID, limit = 5)
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun getUserCommentsWithValidPlaceIdReturnsSuccessFromSupabase() = runTest {
        val result = placesRepository.getUserComments(KNOWN_LOCATION_ID)
        assertTrue(result.isSuccess)
    }

    @Test
    fun getUserCommentsWithEmptyPlaceIdReturnsFailure() = runTest {
        val result = placesRepository.getUserComments("")
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserCommentsStatsWithValidPlaceIdReturnsSuccess() = runTest {
        val result = placesRepository.getUserCommentsStats(KNOWN_LOCATION_ID)
        assertTrue(result.isSuccess)
        val (_, count) = result.getOrNull()!!
        assertTrue(count >= 0)
    }

    @Test
    fun getUserCommentsStatsWithEmptyPlaceIdReturnsZeroCount() = runTest {
        val result = placesRepository.getUserCommentsStats("")
        assertTrue(result.isSuccess)
        val (_, count) = result.getOrNull()!!
        assertTrue(count == 0)
    }
}
