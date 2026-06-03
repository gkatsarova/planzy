package com.planzy.app.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryIntegrationTest : BaseRepositoryIntegrationTest() {

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        authRepository = AuthRepositoryImpl(
            resourceProvider = getResourceProvider()
        )
    }

    @Test
    fun signInWithInvalidCredentialsReturnsFailureFromSupabase() = runTest {
        val result = authRepository.signIn(
            email = "nonexistentuser@test.com",
            password = "wrongpassword123"
        )
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertFalse(result.exceptionOrNull()?.message.isNullOrEmpty())
    }

    @Test
    fun signInWithEmptyEmailReturnsFailureGracefully() = runTest {
        val result = authRepository.signIn(email = "", password = "Somepassword1!")
        assertTrue(result.isFailure)
    }

    @Test
    fun signUpWithInvalidEmailFormatReturnsFailureFromSupabase() = runTest {
        val result = authRepository.signUp(
            email = "not-a-valid-email",
            password = "Password123!",
            username = "testuser"
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun signUpWithShortPasswordReturnsFailureFromSupabase() = runTest {
        val result = authRepository.signUp(
            email = "valid_format@test.com",
            password = "123",
            username = "testuser"
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun checkUsernameExistsWithNonExistentUsernameReturnsFalse() = runTest {
        val result = authRepository.checkUsernameExists("nonexistent___")
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
    }

    @Test
    fun checkEmailExistsInAuthWithNonExistentEmailReturnsFalse() = runTest {
        val result = authRepository.checkEmailExistsInAuth("nonexistent@test.com")
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
    }

    @Test
    fun getCurrentUserWithoutSessionReturnsNull() = runTest {
        val user = authRepository.getCurrentUser()
        assertNull(user)
    }

    @Test
    fun sendPasswordResetEmailWithValidFormatCommunicatesWithSupabase() = runTest {
        val result = authRepository.sendPasswordResetEmail("nonexistent@test.com")
        assertNotNull(result)
    }
}
