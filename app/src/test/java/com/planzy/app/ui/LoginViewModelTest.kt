package com.planzy.app.ui

import com.planzy.app.R
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.data.model.User
import com.planzy.app.domain.repository.AuthRepository
import com.planzy.app.domain.usecase.auth.LoginUseCase
import com.planzy.app.domain.usecase.auth.ResendVerificationEmailUseCase
import com.planzy.app.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.planzy.app.domain.usecase.auth.UpdatePasswordUseCase
import com.planzy.app.domain.usecase.user.GetUserByAuthIdUseCase
import com.planzy.app.ui.screens.auth.login.LoginViewModel
import io.github.jan.supabase.SupabaseClient as SupabaseClientType
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var resendVerificationEmailUseCase: ResendVerificationEmailUseCase
    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
    private lateinit var getUserByAuthIdUseCase: GetUserByAuthIdUseCase
    private lateinit var updatePasswordUseCase: UpdatePasswordUseCase
    private lateinit var authRepository: AuthRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var cooldownManager: CooldownManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        loginUseCase = mockk(relaxed = true)
        resendVerificationEmailUseCase = mockk(relaxed = true)
        sendPasswordResetEmailUseCase = mockk(relaxed = true)
        getUserByAuthIdUseCase = mockk(relaxed = true)
        updatePasswordUseCase = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        resourceProvider = mockk(relaxed = true)
        cooldownManager = mockk(relaxed = true)

        setupResourceStrings()
        setupSupabaseClient()

        viewModel = LoginViewModel(
            loginUseCase,
            resendVerificationEmailUseCase,
            sendPasswordResetEmailUseCase,
            updatePasswordUseCase,
            getUserByAuthIdUseCase,
            authRepository,
            resourceProvider,
            cooldownManager
        )
    }

    private fun setupResourceStrings() {
        every { resourceProvider.getString(R.string.error_email_invalid) } returns
                "Invalid email format."
        every { resourceProvider.getString(R.string.error_password_invalid) } returns
                "Password must be at least 8 characters including lowercase, uppercase, numbers and special symbols."
        every { resourceProvider.getString(R.string.error_email_not_verified) } returns
                "Email not verified"
        every { resourceProvider.getString(R.string.error_email_not_found) } returns
                "Email not found"
        every { resourceProvider.getString(R.string.error_passwords_dont_match) } returns
                "Passwords don't match"
    }

    private fun setupSupabaseClient() {
        val mockUserInfo = mockk<UserInfo>(relaxed = true)
        every { mockUserInfo.id } returns "test-user-id"

        val mockAuth = mockk<Auth>(relaxed = true)
        every { mockAuth.currentUserOrNull() } returns mockUserInfo

        val mockSupabaseClient = mockk<SupabaseClientType>(relaxed = true)

        mockkStatic("io.github.jan.supabase.auth.AuthKt")
        every { mockSupabaseClient.auth } returns mockAuth

        mockkObject(SupabaseClient)
        every { SupabaseClient.client } returns mockSupabaseClient

        coEvery { getUserByAuthIdUseCase(any()) } returns Result.success(
            mockk<User>(relaxed = true) {
                every { profilePictureUrl } returns null
            }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(SupabaseClient)
        unmockkStatic("io.github.jan.supabase.auth.AuthKt")
    }

    @Test
    fun `successful login shows success state`() = runTest {
        coEvery { loginUseCase("test@example.com", "Password123!") } returns
                Result.success("Login successful")

        viewModel.login("test@example.com", "Password123!")
        advanceUntilIdle()

        Assert.assertTrue(viewModel.success.value)
        Assert.assertNull(viewModel.error.value)
        Assert.assertFalse(viewModel.loading.value)
    }

    @Test
    fun `failed login shows error message`() = runTest {
        val errorMessage = "Invalid credentials"
        coEvery { loginUseCase(any(), any()) } returns
                Result.failure(Exception(errorMessage))

        viewModel.login("test@example.com", "WrongPass!")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.success.value)
        Assert.assertEquals(errorMessage, viewModel.error.value)
    }

    @Test
    fun `sendPasswordResetEmail succeeds for existing email`() = runTest {
        coEvery { authRepository.checkEmailExistsInAuth("test@example.com") } returns
                Result.success(true)
        coEvery { sendPasswordResetEmailUseCase("test@example.com") } returns
                Result.success("Password reset email sent")

        viewModel.sendPasswordResetEmail("test@example.com")
        advanceUntilIdle()

        Assert.assertTrue(viewModel.forgotPasswordSuccess.value)
        Assert.assertNotNull(viewModel.forgotPasswordMessage.value)
        Assert.assertFalse(viewModel.forgotPasswordLoading.value)
    }

    @Test
    fun `sendPasswordResetEmail fails for non-existing email`() = runTest {
        coEvery { authRepository.checkEmailExistsInAuth("nonexistent@example.com") } returns
                Result.success(false)

        viewModel.sendPasswordResetEmail("nonexistent@example.com")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.forgotPasswordSuccess.value)
        Assert.assertEquals("Email not found", viewModel.error.value)
    }

    @Test
    fun `sendPasswordResetEmail handles repository failure`() = runTest {
        coEvery { authRepository.checkEmailExistsInAuth("test@example.com") } returns
                Result.success(true)
        coEvery { sendPasswordResetEmailUseCase(any()) } returns
                Result.failure(Exception("Network error"))

        viewModel.sendPasswordResetEmail("test@example.com")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.forgotPasswordSuccess.value)
        Assert.assertNotNull(viewModel.error.value)
    }

    @Test
    fun `clearForgotPassword resets state`() {
        viewModel.clearForgotPassword()

        Assert.assertFalse(viewModel.forgotPasswordLoading.value)
        Assert.assertFalse(viewModel.forgotPasswordSuccess.value)
        Assert.assertNull(viewModel.forgotPasswordMessage.value)
    }

    @Test
    fun `enableResetPasswordMode activates reset mode`() {
        viewModel.enableResetPasswordMode()

        Assert.assertTrue(viewModel.isResetPasswordMode.value)
    }

    @Test
    fun `resetPassword succeeds with matching valid passwords`() = runTest {
        coEvery { updatePasswordUseCase("NewPassword123!") } returns
                Result.success("Password updated")

        viewModel.resetPassword("NewPassword123!", "NewPassword123!")
        advanceUntilIdle()

        Assert.assertTrue(viewModel.success.value)
        Assert.assertFalse(viewModel.resetPasswordLoading.value)
    }

    @Test
    fun `resetPassword fails with mismatched passwords`() = runTest {
        viewModel.resetPassword("NewPassword123!", "DifferentPass123!")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.success.value)
        Assert.assertEquals("Passwords don't match", viewModel.error.value)
    }

    @Test
    fun `resetPassword fails with invalid password`() = runTest {
        viewModel.resetPassword("weak", "weak")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.success.value)
        Assert.assertNotNull(viewModel.error.value)
    }

    @Test
    fun `resetPassword handles use case failure`() = runTest {
        coEvery { updatePasswordUseCase(any()) } returns
                Result.failure(Exception("Update failed"))

        viewModel.resetPassword("NewPassword123!", "NewPassword123!")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.success.value)
        Assert.assertEquals("Update failed", viewModel.error.value)
    }

    @Test
    fun `validateNewPassword updates field error state`() {
        viewModel.validateNewPassword("weak")
        Assert.assertNotNull(viewModel.newPasswordError.value)

        viewModel.validateNewPassword("StrongPass123!")
        Assert.assertNull(viewModel.newPasswordError.value)
    }

    @Test
    fun `validateConfirmPassword updates field error state`() {
        viewModel.validateConfirmPassword("Password123!", "DifferentPass123!")
        Assert.assertNotNull(viewModel.confirmPasswordError.value)

        viewModel.validateConfirmPassword("Password123!", "Password123!")
        Assert.assertNull(viewModel.confirmPasswordError.value)
    }
}