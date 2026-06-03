package com.planzy.app.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planzy.app.data.remote.SupabaseClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VacationRepositoryIntegrationTest : BaseRepositoryIntegrationTest() {

    private lateinit var vacationRepository: VacationsRepositoryImpl

    companion object {
        const val DUMMY_UUID = "00000000-0000-0000-0000-000000000000"
    }

    @Before
    fun setup() {
        vacationRepository = VacationsRepositoryImpl(
            supabaseClient = SupabaseClient,
            resourceProvider = getResourceProvider()
        )
    }


    @Test
    fun searchVacationsWithEmptyQueryReturnsSuccessResult() = runTest {
        val result = vacationRepository.searchVacations(query = "")
        assertTrue(result.isSuccess)
    }

    @Test
    fun getUserVacationsWithoutAuthReturnsFailure() = runTest {
        val result = vacationRepository.getUserVacations()
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
    }

    @Test
    fun createVacationWithoutAuthReturnsFailure() = runTest {
        val result = vacationRepository.createVacation("Test Vacation")
        assertTrue(result.isFailure)
    }

    @Test
    fun getVacationPlaceIdsWithDummyIdReturnsEmptyList() = runTest {
        val result = vacationRepository.getVacationPlaceIds(DUMMY_UUID)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun getVacationCommentsWithDummyIdReturnsEmptyList() = runTest {
        val result = vacationRepository.getVacationComments(DUMMY_UUID)
        assertTrue(result.isSuccess)
        val comments = result.getOrNull()
        assertNotNull(comments)
        assertTrue(comments!!.isEmpty())
    }

    @Test
    fun getVacationCommentsCountWithDummyIdReturnsZero() = runTest {
        val result = vacationRepository.getVacationCommentsCount(DUMMY_UUID)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == 0)
    }

    @Test
    fun isVacationSavedWithoutAuthReturnsFailure() = runTest {
        val result = vacationRepository.isVacationSaved(DUMMY_UUID)
        assertTrue(result.isFailure)
    }

    @Test
    fun getSavedVacationsWithoutAuthReturnsFailure() = runTest {
        val result = vacationRepository.getSavedVacations()
        assertTrue(result.isFailure)
    }

    @Test
    fun getVacationWithUserWithoutAuthReturnsFailure() = runTest {
        val result = vacationRepository.getVacationWithUser(DUMMY_UUID)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserVacationsByIdWithDummyIdReturnsEmptyList() = runTest {
        val result = vacationRepository.getUserVacationsById(DUMMY_UUID)
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull() == null)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}
