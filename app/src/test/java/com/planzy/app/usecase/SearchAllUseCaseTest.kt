package com.planzy.app.usecase

import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.util.LocationEntityExtractor
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Location
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.UserRepository
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.model.SearchAllOutcome
import com.planzy.app.domain.model.SearchAllParams
import com.planzy.app.domain.usecase.search.SearchAllUseCase
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchAllUseCaseTest {

    private lateinit var placesRepository: PlacesRepository
    private lateinit var vacationsRepository: VacationsRepository
    private lateinit var userRepository: UserRepository
    private lateinit var entityExtractor: LocationEntityExtractor
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var useCase: SearchAllUseCase

    private val samplePlace = Place(
        id = "p1",
        name = "Eiffel Tower",
        location = Location(48.85, 2.29, "Paris, France"),
        rating = 4.8,
        reviewsCount = 5000,
        description = null,
        photoUrl = null,
        category = "Attraction",
        contact = null
    )

    private val sampleVacation = Vacation(
        id = "v1",
        userId = "u1",
        title = "Paris Trip",
        createdAt = "2025-01-01",
        placesCount = 3,
        commentsCount = 0
    )

    private val sampleUser = User(
        id = 1,
        auth_id = "auth1",
        username = "alice",
        email = "alice@example.com"
    )

    @Before
    fun setup() {
        placesRepository = mockk()
        vacationsRepository = mockk()
        userRepository = mockk()
        entityExtractor = mockk()
        resourceProvider = mockk(relaxed = true)

        every { resourceProvider.getString(R.string.error_no_results_found) } returns "No results found"
        every { resourceProvider.getString(R.string.error_api_limit) } returns "API limit"
        every { resourceProvider.getString(R.string.error_unauthorized) } returns "Unauthorized"
        every { resourceProvider.getString(R.string.error_no_internet) } returns "No internet"
        every { resourceProvider.getString(R.string.error_unknown) } returns "Unknown error"

        coEvery { entityExtractor.extractLocation(any()) } returns null
    }

    @After
    fun tearDown() = clearAllMocks()

    private fun createUseCase() = SearchAllUseCase(
        placesRepository, vacationsRepository, userRepository, entityExtractor, resourceProvider
    )

    @Test
    fun `blank query returns Empty outcome without calling repositories`() = runTest {
        useCase = createUseCase()
        val result = useCase(SearchAllParams("  "))

        assertTrue(result is SearchAllOutcome.Empty)
        coVerify(exactly = 0) { placesRepository.searchPlaces(any(), any(), any(), any()) }
        coVerify(exactly = 0) { vacationsRepository.searchVacations(any()) }
        coVerify(exactly = 0) { userRepository.searchUsers(any()) }
    }

    @Test
    fun `all three sources return results Success outcome`() = runTest {
        coEvery { placesRepository.searchPlaces("Paris", any(), any(), any()) } returns
                Result.success(listOf(samplePlace))
        coEvery { vacationsRepository.searchVacations("Paris") } returns
                Result.success(listOf(sampleVacation))
        coEvery { userRepository.searchUsers("Paris") } returns
                Result.success(listOf(sampleUser))

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("Paris"))

        assertTrue(outcome is SearchAllOutcome.Success)
        val data = (outcome as SearchAllOutcome.Success).result
        assertEquals(1, data.places.size)
        assertEquals(1, data.vacations.size)
        assertEquals(1, data.users.size)
    }

    @Test
    fun `all sources empty returns Empty outcome`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.success(emptyList())
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("Nowhere"))

        assertTrue(outcome is SearchAllOutcome.Empty)
    }

    @Test
    fun `places fail but vacations succeed PlacesError outcome with partial data`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.failure(Exception("429"))
        coEvery { vacationsRepository.searchVacations(any()) } returns
                Result.success(listOf(sampleVacation))
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("trip"))

        assertTrue(outcome is SearchAllOutcome.PlacesError)
        val partial = (outcome as SearchAllOutcome.PlacesError).partialResult
        assertEquals(1, partial.vacations.size)
        assertTrue(partial.places.isEmpty())
        assertEquals("API limit", outcome.message)
    }

    @Test
    fun `users fail silently and remaining results still returned`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.success(listOf(samplePlace))
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.failure(Exception("DB error"))

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("tower"))

        assertTrue(outcome is SearchAllOutcome.Success)
        val data = (outcome as SearchAllOutcome.Success).result
        assertEquals(1, data.places.size)
        assertTrue(data.users.isEmpty())
    }

    @Test
    fun `places are sorted by rating descending and capped at 10`() = runTest {
        val many = (1..15).map { i ->
            samplePlace.copy(id = "p$i", rating = i.toDouble())
        }
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.success(many)
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("place"))

        val data = (outcome as SearchAllOutcome.Success).result
        assertEquals(10, data.places.size)
        assertEquals(15.0, data.places.first().rating, 0.0)
        assertEquals(6.0, data.places.last().rating, 0.0)
    }

    @Test
    fun `no location in text AND permission granted GPS params forwarded to repository`() = runTest {
        coEvery { entityExtractor.extractLocation(any()) } returns null
        coEvery {
            placesRepository.searchPlaces("bar", any(), "48.0,2.0", 25)
        } returns Result.success(emptyList())
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        useCase(
            SearchAllParams(
                query = "bar",
                userLocation = Pair(48.0, 2.0),
                locationPermissionGranted = true
            )
        )

        coVerify { placesRepository.searchPlaces("bar", any(), "48.0,2.0", 25) }
    }

    @Test
    fun `city detected in query GPS params NOT used despite permission granted`() = runTest {
        val mockAnnotation = mockk<EntityAnnotation>()
        val mockEntity = mockk<Entity>()
        every { mockEntity.type } returns Entity.TYPE_ADDRESS
        every { mockAnnotation.entities } returns listOf(mockEntity)
        coEvery { entityExtractor.extractLocation("Paris") } returns mockAnnotation

        coEvery {
            placesRepository.searchPlaces("bars in Paris", any(), null, null)
        } returns Result.success(emptyList())
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        useCase(
            SearchAllParams(
                query = "bars in Paris",
                userLocation = Pair(48.0, 2.0),
                locationPermissionGranted = true
            )
        )

        coVerify { placesRepository.searchPlaces("bars in Paris", any(), null, null) }
    }

    @Test
    fun `second call with same query returns cached result without hitting repositories`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.success(listOf(samplePlace))
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        useCase(SearchAllParams("Paris"))
        useCase(SearchAllParams("Paris"))

        coVerify(exactly = 1) { placesRepository.searchPlaces(any(), any(), any(), any()) }
        coVerify(exactly = 1) { vacationsRepository.searchVacations(any()) }
        coVerify(exactly = 1) { userRepository.searchUsers(any()) }
    }

    @Test
    fun `clearCache forces re-fetch on next invocation`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.success(listOf(samplePlace))
        coEvery { vacationsRepository.searchVacations(any()) } returns Result.success(emptyList())
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        useCase(SearchAllParams("Paris"))
        useCase.clearCache()
        useCase(SearchAllParams("Paris"))

        coVerify(exactly = 2) { placesRepository.searchPlaces(any(), any(), any(), any()) }
    }

    @Test
    fun `429 error maps to API limit message`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.failure(Exception("429"))
        coEvery { vacationsRepository.searchVacations(any()) } returns
                Result.success(listOf(sampleVacation))
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("test"))

        assertTrue(outcome is SearchAllOutcome.PlacesError)
        assertEquals("API limit", (outcome as SearchAllOutcome.PlacesError).message)
    }

    @Test
    fun `network error maps to no internet message`() = runTest {
        coEvery { placesRepository.searchPlaces(any(), any(), any(), any()) } returns
                Result.failure(Exception("Unable to resolve host"))
        coEvery { vacationsRepository.searchVacations(any()) } returns
                Result.success(listOf(sampleVacation))
        coEvery { userRepository.searchUsers(any()) } returns Result.success(emptyList())

        useCase = createUseCase()
        val outcome = useCase(SearchAllParams("test"))

        assertTrue(outcome is SearchAllOutcome.PlacesError)
        assertEquals("No internet", (outcome as SearchAllOutcome.PlacesError).message)
    }
}