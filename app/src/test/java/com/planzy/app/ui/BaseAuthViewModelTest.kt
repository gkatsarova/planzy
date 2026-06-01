package com.planzy.app.ui

import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.ui.screens.auth.BaseAuthViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BaseAuthViewModelTest {

    private lateinit var viewModel: TestAuthViewModel
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var cooldownManager: CooldownManager

    private val testDispatcher = StandardTestDispatcher()

    private class TestAuthViewModel(
        resourceProvider: ResourceProvider,
        cooldownManager: CooldownManager
    ) : BaseAuthViewModel(resourceProvider, cooldownManager)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        resourceProvider = mockk(relaxed = true)
        cooldownManager = mockk(relaxed = true)

        every { cooldownManager.getRemainingCooldownSeconds() } returns 0

        viewModel = TestAuthViewModel(resourceProvider, cooldownManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `valid email passes validation`() {
        val result = viewModel.isValidEmail("test@example.com")

        Assert.assertTrue(result)
    }

    @Test
    fun `invalid email fails validation`() {
        val invalidEmails = listOf(
            "notanemail",
            "@example.com",
            "test@",
            "test user@example.com"
        )

        invalidEmails.forEach { email ->
            Assert.assertFalse("$email should be invalid", viewModel.isValidEmail(email))
        }
    }

    @Test
    fun `valid password passes validation`() {
        val validPasswords = listOf(
            "Password123!",
            "Abcd1234@",
            "MyP@ssw0rd"
        )

        validPasswords.forEach { password ->
            Assert.assertTrue("$password should be valid", viewModel.isValidPassword(password))
        }
    }

    @Test
    fun `invalid password fails validation`() {
        val invalidPasswords = listOf(
            "short",
            "nouppercase1!",
            "NOLOWERCASE1!",
            "NoNumbers!",
            "NoSpecial123",
            "Pass123"
        )

        invalidPasswords.forEach { password ->
            Assert.assertFalse("$password should be invalid", viewModel.isValidPassword(password))
        }
    }

    @Test
    fun `clearError resets error state`() {
        viewModel.setAuthError("Test error")

        viewModel.clearError()

        Assert.assertNull(viewModel.error)
    }

    @Test
    fun `clearSuccess resets success message`() = runTest {
        viewModel.clearSuccess()

        Assert.assertNull(viewModel.successMessage)
    }

    @Test
    fun `setAuthError updates error and clears success states`() {
        viewModel.setAuthError("Test error")

        Assert.assertEquals("Test error", viewModel.error)
        Assert.assertFalse(viewModel.success)
        Assert.assertNull(viewModel.successMessage)
    }

    @Test
    fun `existing cooldown is restored on init`() {
        every { cooldownManager.getRemainingCooldownSeconds() } returns 30

        val newViewModel = TestAuthViewModel(resourceProvider, cooldownManager)

        Assert.assertFalse(newViewModel.canResendEmail)
        Assert.assertEquals(30, newViewModel.resendCooldownSeconds)
    }

    @Test
    fun `no existing cooldown starts with enabled resend`() {
        every { cooldownManager.getRemainingCooldownSeconds() } returns 0

        val newViewModel = TestAuthViewModel(resourceProvider, cooldownManager)

        Assert.assertTrue(newViewModel.canResendEmail)
        Assert.assertEquals(0, newViewModel.resendCooldownSeconds)
    }

    @Test
    fun `loading state starts as false`() {
        Assert.assertFalse(viewModel.loading)
    }

    @Test
    fun `error state starts as null`() {
        Assert.assertNull(viewModel.error)
    }

    @Test
    fun `success message state starts as null`() {
        Assert.assertNull(viewModel.successMessage)
    }

    @Test
    fun `success state starts as false`() {
        Assert.assertFalse(viewModel.success)
    }

    @Test
    fun `canResendEmail state starts as true`() {
        Assert.assertTrue(viewModel.canResendEmail)
    }

    @Test
    fun `resendCooldownSeconds state starts as zero`() {
        Assert.assertEquals(0, viewModel.resendCooldownSeconds)
    }
}