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
    fun `getVacationDetails successful fetch returns vacation details with all places`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns
                Result.success(Pair(mockVacation, "john_doe"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns
                Result.success(listOf("place1", "place2"))
        coEvery { placesRepository.getPlaceDetails("place1") } returns Result.success(mockPlace1)
        coEvery { placesRepository.getPlaceDetails("place2") } returns Result.success(mockPlace2)

        val result = useCase.getVacationDetails("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!
        assertEquals(mockVacation, details.vacation)
        assertEquals("john_doe", details.creatorUsername)
        assertEquals(2, details.places.size)
        assertEquals(mockPlace1, details.places[0])
        assertEquals(mockPlace2, details.places[1])
    }

    @Test
    fun `getVacationDetails vacation with no places returns empty list`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns
                Result.success(Pair(mockVacation, "john_doe"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns
                Result.success(emptyList())

        val result = useCase.getVacationDetails("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!
        assertEquals(0, details.places.size)
        assertEquals("john_doe", details.creatorUsername)
    }

    @Test
    fun `getVacationDetails exception during execution returns failure`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } throws
                RuntimeException("Network error")

        val result = useCase.getVacationDetails("vacation123")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getVacationDetails preserves place order from vacation place ids`() = runTest {
        coEvery { vacationsRepository.getVacationWithUser("vacation123") } returns
                Result.success(Pair(mockVacation, "alice"))
        coEvery { vacationsRepository.getVacationPlaceIds("vacation123") } returns
                Result.success(listOf("place2", "place1"))
        coEvery { placesRepository.getPlaceDetails("place1") } returns Result.success(mockPlace1)
        coEvery { placesRepository.getPlaceDetails("place2") } returns Result.success(mockPlace2)

        val result = useCase.getVacationDetails("vacation123")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()!!
        assertEquals(mockPlace2, details.places[0])
        assertEquals(mockPlace1, details.places[1])
    }

    @Test
    fun `getVacationComments success returns list of comments`() = runTest {
        val mockComment1 = mockk<VacationComment>(relaxed = true)
        val mockComment2 = mockk<VacationComment>(relaxed = true)

        coEvery { vacationsRepository.getVacationComments("vacation123") } returns
                Result.success(listOf(mockComment1, mockComment2))

        val result = useCase.getVacationComments("vacation123")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `getVacationComments returns empty list when there are no comments`() = runTest {
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns
                Result.success(emptyList())

        val result = useCase.getVacationComments("vacation123")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getVacationComments failure returns failure`() = runTest {
        val exception = Exception("Comments not available")
        coEvery { vacationsRepository.getVacationComments("vacation123") } returns
                Result.failure(exception)

        val result = useCase.getVacationComments("vacation123")

        assertTrue(result.isFailure)
        assertEquals("Comments not available", result.exceptionOrNull()?.message)
    }
}
