package com.planzy.app.ui

import com.planzy.app.R
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.usecase.CheckEmailAvailabilityUseCase
import com.planzy.app.domain.usecase.CheckUsernameAvailabilityUseCase
import com.planzy.app.domain.usecase.RegisterUserUseCase
import com.planzy.app.domain.usecase.ResendVerificationEmailUseCase
import com.planzy.app.ui.screens.registration.RegisterViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel
    private lateinit var registerUserUseCase: RegisterUserUseCase
    private lateinit var checkUsernameAvailabilityUseCase: CheckUsernameAvailabilityUseCase
    private lateinit var checkEmailAvailabilityUseCase: CheckEmailAvailabilityUseCase
    private lateinit var resendVerificationEmailUseCase: ResendVerificationEmailUseCase
    private lateinit var resourceProvider: ResourceProvider

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        registerUserUseCase = mockk(relaxed = true)
        checkUsernameAvailabilityUseCase = mockk(relaxed = true)
        checkEmailAvailabilityUseCase = mockk(relaxed = true)
        resendVerificationEmailUseCase = mockk(relaxed = true)
        resourceProvider = mockk(relaxed = true)

        setupResourceStrings()

        viewModel = RegisterViewModel(
            registerUserUseCase,
            checkUsernameAvailabilityUseCase,
            checkEmailAvailabilityUseCase,
            resendVerificationEmailUseCase,
            resourceProvider
        )
    }

    private fun setupResourceStrings() {
        every { resourceProvider.getString(R.string.error_username_invalid) } returns
                "Username must be 3–20 symbols, including lowercase, numbers and special symbols(underscore, dot,)."
        every { resourceProvider.getString(R.string.error_username_exists) } returns
                "This username already exists"
        every { resourceProvider.getString(R.string.error_email_invalid) } returns
                "Invalid email format."
        every { resourceProvider.getString(R.string.error_email_exists) } returns
                "This email is already registered. Please try logging in or use password reset."
        every { resourceProvider.getString(R.string.error_password_invalid) } returns
                "Password must be at least 8 characters including lowercase, uppercase, numbers and special symbols."
        every { resourceProvider.getString(R.string.verification_email) } returns
                "Verification email"
        every { resourceProvider.getString(R.string.success_verification_email_sent) } returns
                "Verification email sent. Please check your inbox."
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful registration shows success state`() = runTest {
        coEvery { registerUserUseCase(any(), any(), any()) } returns
                Result.success("Verification email sent")

        viewModel.signUp("test@example.com", "Password123!", "testuser")
        advanceUntilIdle()

        Assert.assertTrue(viewModel.success.value)
        Assert.assertNotNull(viewModel.successMessage.value)
    }

    @Test
    fun `failed registration shows error message`() = runTest {
        val errorMessage = "Network error"
        coEvery { registerUserUseCase(any(), any(), any()) } returns
                Result.failure(Exception(errorMessage))

        viewModel.signUp("test@example.com", "Password123!", "testuser")
        advanceUntilIdle()

        Assert.assertFalse(viewModel.success.value)
        Assert.assertEquals(errorMessage, viewModel.error.value)
    }

    @Test
    fun `username with uppercase letters is rejected`() = runTest {
        viewModel.validateUsername("TestUser")
        advanceUntilIdle()

        Assert.assertNotNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `available username passes validation`() = runTest {
        coEvery { checkUsernameAvailabilityUseCase("validuser") } returns Result.success(true)

        viewModel.validateUsername("validuser")
        advanceUntilIdle()

        Assert.assertNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `taken username shows error`() = runTest {
        coEvery { checkUsernameAvailabilityUseCase("existinguser") } returns Result.success(false)

        viewModel.validateUsername("existinguser")
        advanceUntilIdle()

        Assert.assertEquals(
            "This username already exists",
            viewModel.fieldErrors.value.usernameError
        )
    }

    @Test
    fun `malformed email is rejected`() = runTest {
        viewModel.validateEmail("notanemail")
        advanceUntilIdle()

        Assert.assertNotNull(viewModel.fieldErrors.value.emailError)
    }

    @Test
    fun `valid available email passes validation`() = runTest {
        coEvery { checkEmailAvailabilityUseCase("test@example.com") } returns Result.success(true)

        viewModel.validateEmail("test@example.com")
        advanceUntilIdle()

        Assert.assertNull(viewModel.fieldErrors.value.emailError)
    }

    @Test
    fun `registered email shows error`() = runTest {
        coEvery { checkEmailAvailabilityUseCase("existing@example.com") } returns Result.success(
            false
        )

        viewModel.validateEmail("existing@example.com")
        advanceUntilIdle()

        Assert.assertNotNull(viewModel.fieldErrors.value.emailError)
    }

    @Test
    fun `weak password is rejected`() {
        viewModel.validatePassword("weak")

        Assert.assertNotNull(viewModel.fieldErrors.value.passwordError)
    }

    @Test
    fun `strong password passes validation`() {
        viewModel.validatePassword("Password123!")

        Assert.assertNull(viewModel.fieldErrors.value.passwordError)
    }

    @Test
    fun `resending verification email succeeds`() = runTest {
        coEvery { resendVerificationEmailUseCase("test@example.com") } returns
                Result.success("Email resent")

        viewModel.resendVerificationEmail("test@example.com")
        advanceUntilIdle()

        Assert.assertNotNull(viewModel.successMessage.value)
        Assert.assertFalse(viewModel.loading.value)
    }

    @Test
    fun `failed email resend shows error`() = runTest {
        coEvery { resendVerificationEmailUseCase(any()) } returns
                Result.failure(Exception("Network error"))

        viewModel.resendVerificationEmail("test@example.com")
        advanceUntilIdle()

        Assert.assertNotNull(viewModel.error.value)
    }

    @Test
    fun `empty username clears error`() = runTest {
        viewModel.validateUsername("")
        advanceUntilIdle()

        Assert.assertNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `minimum length username is accepted`() = runTest {
        coEvery { checkUsernameAvailabilityUseCase("abc") } returns Result.success(true)

        viewModel.validateUsername("abc")
        advanceUntilIdle()

        Assert.assertNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `maximum length username is accepted`() = runTest {
        val longUsername = "a".repeat(20)
        coEvery { checkUsernameAvailabilityUseCase(longUsername) } returns Result.success(true)

        viewModel.validateUsername(longUsername)
        advanceUntilIdle()

        Assert.assertNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `too long username is rejected`() = runTest {
        viewModel.validateUsername("a".repeat(21))
        advanceUntilIdle()

        Assert.assertNotNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `username with special characters is accepted`() = runTest {
        coEvery { checkUsernameAvailabilityUseCase("user.name_123") } returns Result.success(true)

        viewModel.validateUsername("user.name_123")
        advanceUntilIdle()

        Assert.assertNull(viewModel.fieldErrors.value.usernameError)
    }

    @Test
    fun `clearError resets error state`() {
        viewModel.clearError()

        Assert.assertNull(viewModel.error.value)
    }

    @Test
    fun `clearSuccess resets success state`() {
        viewModel.clearSuccess()

        Assert.assertFalse(viewModel.success.value)
        Assert.assertNull(viewModel.successMessage.value)
    }
}