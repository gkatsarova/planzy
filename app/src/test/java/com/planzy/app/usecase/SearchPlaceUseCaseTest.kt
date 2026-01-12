package com.planzy.app.usecase

import com.planzy.app.domain.model.ContactInfo
import com.planzy.app.domain.model.Location
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.usecase.place.SearchPlacesUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SearchPlacesUseCaseTest {

    private lateinit var placesRepository: PlacesRepository
    private lateinit var useCase: SearchPlacesUseCase

    private val samplePlace1 = Place(
        id = "1",
        name = "Restaurant A",
        location = Location(42.6977, 23.3219, "Sofia, Bulgaria"),
        rating = 4.5,
        reviewsCount = 120,
        description = "Great restaurant",
        photoUrl = "https://example.com/photo1.jpg",
        category = "Restaurant",
        contact = ContactInfo(phone = "+359123456", website = "https://example.com")
    )

    private val samplePlace2 = Place(
        id = "2",
        name = "Cafe B",
        location = Location(42.6978, 23.3220, "Sofia, Bulgaria"),
        rating = 4.8,
        reviewsCount = 85,
        description = "Cozy cafe",
        photoUrl = "https://example.com/photo2.jpg",
        category = "Cafe",
        contact = ContactInfo(phone = "+359654321", website = "https://example2.com")
    )

    private val samplePlace3 = Place(
        id = "3",
        name = "Bar C",
        location = Location(42.6979, 23.3221, "Sofia, Bulgaria"),
        rating = 3.5,
        reviewsCount = 45,
        description = "Nice bar",
        photoUrl = "https://example.com/photo3.jpg",
        category = "Bar",
        contact = null
    )

    @Before
    fun setup() {
        placesRepository = mockk()
        useCase = SearchPlacesUseCase(placesRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `successful search returns sorted places by rating`() = runTest {
        val places = listOf(samplePlace1, samplePlace3, samplePlace2)
        coEvery {
            placesRepository.searchPlaces(
                query = "Sofia",
                minRating = 3.0,
                latLong = null,
                radius = null
            )
        } returns Result.success(places)

        val result = useCase(destination = "Sofia")

        Assert.assertTrue(result.isSuccess)
        val resultPlaces = result.getOrNull()
        Assert.assertEquals(3, resultPlaces?.size)
        Assert.assertEquals(4.8, resultPlaces?.first()?.rating ?: 0.0, 0.01)
        Assert.assertEquals("Cafe B", resultPlaces?.first()?.name)
    }

    @Test
    fun `search respects maxResults parameter`() = runTest {
        val places = listOf(samplePlace1, samplePlace2, samplePlace3)
        coEvery {
            placesRepository.searchPlaces(
                query = "Sofia",
                minRating = 3.0,
                latLong = null,
                radius = null
            )
        } returns Result.success(places)

        val result = useCase(destination = "Sofia", maxResults = 2)

        Assert.assertTrue(result.isSuccess)
        val resultPlaces = result.getOrNull()
        Assert.assertEquals(2, resultPlaces?.size)
    }

    @Test
    fun `search with custom minRating parameter`() = runTest {
        val places = listOf(samplePlace1, samplePlace2)
        coEvery {
            placesRepository.searchPlaces(
                query = "Sofia",
                minRating = 4.0,
                latLong = null,
                radius = null
            )
        } returns Result.success(places)

        val result = useCase(destination = "Sofia", minRating = 4.0)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `search with latLong and radius parameters`() = runTest {
        val places = listOf(samplePlace1)
        coEvery {
            placesRepository.searchPlaces(
                query = "Sofia",
                minRating = 3.0,
                latLong = "42.6977,23.3219",
                radius = 1000
            )
        } returns Result.success(places)

        val result = useCase(
            destination = "Sofia",
            latLong = "42.6977,23.3219",
            radius = 1000
        )

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `failed search returns error`() = runTest {
        coEvery {
            placesRepository.searchPlaces(
                query = any(),
                minRating = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.failure(Exception("Network error"))

        val result = useCase(destination = "Sofia")

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `empty search results returns empty list`() = runTest {
        coEvery {
            placesRepository.searchPlaces(
                query = "NonExistent",
                minRating = 3.0,
                latLong = null,
                radius = null
            )
        } returns Result.success(emptyList())

        val result = useCase(destination = "NonExistent")

        Assert.assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull()?.isEmpty() ?: false)
    }

    @Test
    fun `repository exception returns error`() = runTest {
        coEvery {
            placesRepository.searchPlaces(
                query = any(),
                minRating = any(),
                latLong = any(),
                radius = any()
            )
        } returns Result.failure(Exception())

        val result = useCase(destination = "Sofia")

        Assert.assertTrue(result.isFailure)
    }
}