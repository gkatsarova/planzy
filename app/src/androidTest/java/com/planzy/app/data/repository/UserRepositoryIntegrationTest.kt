package com.planzy.app.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryIntegrationTest : BaseRepositoryIntegrationTest() {

    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setup() {
        userRepository = UserRepositoryImpl(
            resourceProvider = getResourceProvider()
        )
    }

    @Test
    fun searchUsersWithValidQueryCommunicatesWithSupabase() = runTest {
        val result = userRepository.searchUsers(query = "test")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun searchUsersWithNonExistentQueryReturnsEmptyList() = runTest {
        val result = userRepository.searchUsers(query = "nonexistent_user")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun searchUsersWithEmptyQueryReturnsSuccessResult() = runTest {
        val result = userRepository.searchUsers(query = "")
        assertTrue(result.isSuccess)
    }

    @Test
    fun getUserByUsernameWithNonExistentUserReturnsNull() = runTest {
        val result = userRepository.getUserByUsername(username = "nonexistent_user")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == null)
    }

    @Test
    fun getUserByAuthIdWithDummyIdReturnsNullSuccessfully() = runTest {
        val result = userRepository.getUserByAuthId(authId = "00000000-0000-0000-0000-000000000000")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == null)
    }

    @Test
    fun uploadProfilePictureWithoutAuthReturnsFailure() = runTest {
        val dummyFile = java.io.File.createTempFile("test", ".jpg")
        dummyFile.deleteOnExit()
        val result = userRepository.uploadProfilePicture(dummyFile)
        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull()?.message.isNullOrEmpty())
    }

    @Test
    fun updateProfilePictureUrlWithoutAuthReturnsFailure() = runTest {
        val result = userRepository.updateProfilePictureUrl("https://example.com/photo.jpg")
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteProfilePictureWithoutAuthReturnsFailure() = runTest {
        val result = userRepository.deleteProfilePicture("https://example.com/photo.jpg")
        assertTrue(result.isFailure)
    }
}
