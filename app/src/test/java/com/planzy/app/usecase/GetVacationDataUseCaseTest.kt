package com.planzy.app.usecase

import com.planzy.app.domain.model.Location
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationComment
import com.planzy.app.domain.repository.PlacesRepository
import com.planzy.app.domain.repository.VacationsRepository
import com.planzy.app.domain.usecase.vacation.GetVacationDataUseCase
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetVacationDataUseCaseTest {

    private lateinit var vacationsRepository: VacationsRepository
    private lateinit var placesRepository: PlacesRepository
    private lateinit var useCase: GetVacationDataUseCase

    private val mockVacation = Vacation(
        id = "vacation123",
        userId = "user456",
        title = "Summer Trip",
        createdAt = "2025-01-01",
        placesCount = 2,
        commentsCount = 5
    )

    private val mockPlace1 = Place(
        id = "place1",
        name = "Eiffel Tower",
        location = Location(48.8584, 2.2945, "Paris, France"),
        rating = 4.5,
        reviewsCount = 1000,
        category = "attraction",
        photoUrl = null,
        description = null,
        contact = null
    )

    private val mockPlace2 = Place(
        id = "place2",
        name = "Louvre Museum",
        location = Location(48.8606, 2.3376, "Paris, France"),
        rating = 4.7,
        reviewsCount = 2000,
        category = "attraction",
        photoUrl = null,
        description = null,
        contact = null
    )

    @Before
    fun setup() {
        vacationsRepository = mockk()
        placesRepository = mockk()
        useCase = GetVacationDataUseCase(vacationsRepository, placesRepository)
    }

    @After
    fun tearDown() = clearAllMocks()

    @Test
    fun `invoke successful fetch returns full vacation details with places and comments`() = runTest {
        val mockComments = listOf(mockk<VacationComment>(relaxed = true), mockk<VacationComment>(relaxed = true))

        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns Result.success(Pair(mockVacation, "john_doe"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns Result.success(listOf("place1", "place2"))
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns Result.success(mockComments)

        coEvery { placesRepository.getPlaceDetails("place1") } returns Result.success(mockPlace1)
        coEvery { placesRepository.getPlaceDetails("place2") } returns Result.success(mockPlace2)

        val result = useCase("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!
        assertEquals(mockVacation, details.vacation)
        assertEquals("john_doe", details.creatorUsername)
        assertEquals(2, details.places.size)
        assertEquals(mockPlace1, details.places[0])
        assertEquals(mockPlace2, details.places[1])
        assertEquals(mockComments, details.vacationComments)
    }

    @Test
    fun `invoke vacation with no places returns empty places list but loads comments`() = runTest {
        val mockComments = listOf(mockk<VacationComment>(relaxed = true))
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns Result.success(Pair(mockVacation, "john_doe"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns Result.success(emptyList())
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns Result.success(mockComments)

        val result = useCase("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!
        assertTrue(details.places.isEmpty())
        assertEquals("john_doe", details.creatorUsername)
        assertEquals(mockComments, details.vacationComments)
    }

    @Test
    fun `invoke failure in core vacation fetching returns failure`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns Result.failure(RuntimeException("Network error"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns Result.success(emptyList())
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns Result.success(emptyList())

        val result = useCase("vacation123")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke failure in comments fetching returns failure`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns Result.success(Pair(mockVacation, "john_doe"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns Result.success(emptyList())
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns Result.failure(RuntimeException("Comments DB error"))

        val result = useCase("vacation123")

        assertTrue(result.isFailure)
        assertEquals("Comments DB error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke preserves place order from vacation place ids`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns Result.success(Pair(mockVacation, "alice"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns Result.success(listOf("place2", "place1"))
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns Result.success(emptyList())
        coEvery { placesRepository.getPlaceDetails("place1") } returns Result.success(mockPlace1)
        coEvery { placesRepository.getPlaceDetails("place2") } returns Result.success(mockPlace2)

        val result = useCase("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!
        assertEquals(mockPlace2, details.places[0])
        assertEquals(mockPlace1, details.places[1])
    }

    @Test
    fun `invoke ignores individual place detail failures gracefully`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns Result.success(Pair(mockVacation, "john_doe"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns Result.success(listOf("place1", "place2"))
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns Result.success(emptyList())

        coEvery { placesRepository.getPlaceDetails("place1") } returns Result.failure(Exception("Place offline"))
        coEvery { placesRepository.getPlaceDetails("place2") } returns Result.success(mockPlace2)

        val result = useCase("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!

        assertEquals(1, details.places.size)
        assertEquals(mockPlace2, details.places[0])
    }
}