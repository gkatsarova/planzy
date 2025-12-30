package com.planzy.app.ui.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.R
import com.planzy.app.data.repository.DeepLinkResult
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.RecoverySessionManager
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.navigation.Register
import com.planzy.app.ui.navigation.Home
import com.planzy.app.ui.screens.auth.registration.DeepLinkViewModel
import com.planzy.app.ui.screens.components.AuthButton
import com.planzy.app.ui.screens.components.InputTextField
import com.planzy.app.ui.screens.components.OutlinedAppButton
import com.planzy.app.ui.screens.components.PasswordTextField
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Raleway
import com.planzy.app.ui.screens.components.MessageCard
import com.planzy.app.ui.screens.components.MessageType
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    deepLinkViewModel: DeepLinkViewModel
) {
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context = context) }
    val recoverySessionManager = remember { RecoverySessionManager(context) }
    val authRepo = remember {
        AuthRepositoryImpl(
            resourceProvider = resourceProvider,
            recoverySessionManager = recoverySessionManager
        )
    }
    val cooldownManager = remember { CooldownManager(context) }

    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(
            authRepository = authRepo,
            resourceProvider = resourceProvider,
            cooldownManager = cooldownManager
        )
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val showResendVerification by viewModel.showResendVerification.collectAsState()
    val canResendEmail by viewModel.canResendEmail.collectAsState()
    val resendCooldownSeconds by viewModel.resendCooldownSeconds.collectAsState()

    val deepLinkResult by deepLinkViewModel.deepLinkResult.collectAsState()

    val forgotPasswordLoading by viewModel.forgotPasswordLoading.collectAsState()
    val forgotPasswordSuccess by viewModel.forgotPasswordSuccess.collectAsState()
    val forgotPasswordMessage by viewModel.forgotPasswordMessage.collectAsState()
    val isResetPasswordMode by viewModel.isResetPasswordMode.collectAsState()
    val resetPasswordLoading by viewModel.resetPasswordLoading.collectAsState()
    val newPasswordError by viewModel.newPasswordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val justResetPassword by viewModel.justResetPassword.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            navController.navigate(Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(deepLinkResult) {
        when (val result = deepLinkResult) {
            is DeepLinkResult.Error -> {
                viewModel.setError(result.message)
                deepLinkViewModel.clearDeepLinkResult()
            }
            is DeepLinkResult.PasswordReset -> {
                viewModel.enableResetPasswordMode()
                deepLinkViewModel.clearDeepLinkResult()
            }
            else -> { }
        }
    }

    LaunchedEffect(Unit) {
        val (savedEmail, savedPassword) = deepLinkViewModel.getPendingCredentials()
        if (savedEmail != null && savedPassword != null) {
            email = savedEmail
            password = savedPassword
        }
    }

    LaunchedEffect(email, password) {
        if (email.isNotBlank() && password.isNotBlank()) {
            deepLinkViewModel.savePendingCredentials(email, password)
        }
    }

    LaunchedEffect(forgotPasswordSuccess) {
        if (forgotPasswordSuccess) {
            delay(5000)
            viewModel.clearForgotPassword()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.auth_screens_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = stringResource(id = R.string.login),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 38.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            InputTextField(
                value = email,
                label = stringResource(id = R.string.email),
                onValueChange = {
                    email = it
                    viewModel.validateEmail(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(70.dp),
                isError = fieldErrors.emailError != null
            )
            fieldErrors.emailError?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isResetPasswordMode || justResetPassword) {
                PasswordTextField(
                    value = newPassword,
                    label = stringResource(id = R.string.new_password),
                    onValueChange = {
                        newPassword = it
                        viewModel.validateNewPassword(it)
                    },
                    isError = newPasswordError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(70.dp)
                )
                newPasswordError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PasswordTextField(
                    value = confirmPassword,
                    label = stringResource(id = R.string.confirm_password),
                    onValueChange = {
                        confirmPassword = it
                        viewModel.validateConfirmPassword(newPassword, it)
                    },
                    isError = confirmPasswordError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(70.dp)
                )
                confirmPasswordError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AuthButton(
                    text = if (justResetPassword) {
                        stringResource(id = R.string.redirecting)
                    } else {
                        stringResource(id = R.string.reset_password)
                    },
                    onClick = {
                        if (!justResetPassword) {
                            viewModel.clearError()
                            viewModel.clearSuccess()
                            viewModel.resetPassword(newPassword, confirmPassword)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(60.dp),
                    enabled = !resetPasswordLoading && !justResetPassword &&
                            newPassword.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            newPasswordError == null &&
                            confirmPasswordError == null,
                    loading = resetPasswordLoading || justResetPassword,
                    loadingText = if (justResetPassword) {
                        stringResource(id = R.string.redirecting)
                    } else {
                        stringResource(id = R.string.resetting)
                    }
                )
            } else {
                PasswordTextField(
                    value = password,
                    label = stringResource(id = R.string.password),
                    onValueChange = {
                        password = it
                        viewModel.validatePassword(it)
                    },
                    isError = fieldErrors.passwordError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(70.dp)
                )
                fieldErrors.passwordError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedAppButton(
                    text = stringResource(id = R.string.forgot_password),
                    onClick = {
                        viewModel.clearError()
                        viewModel.clearForgotPassword()
                        viewModel.sendPasswordResetEmail(email)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(50.dp),
                    enabled = email.isNotBlank() && !forgotPasswordLoading,
                    loading = forgotPasswordLoading,
                    loadingText = stringResource(id = R.string.sending),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthButton(
                    text = stringResource(id = R.string.login),
                    onClick = {
                        viewModel.clearError()
                        viewModel.clearSuccess()
                        viewModel.login(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(60.dp),
                    enabled = !loading &&
                            email.isNotBlank() &&
                            password.isNotBlank() &&
                            fieldErrors.emailError == null &&
                            fieldErrors.passwordError == null,
                    loading = loading,
                    loadingText = stringResource(id = R.string.logging_in)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            error?.let { errorMessage ->
                MessageCard(
                    message = errorMessage,
                    type = MessageType.ERROR,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            forgotPasswordMessage?.let { message ->
                MessageCard(
                    message = message,
                    type = MessageType.SUCCESS,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (success) {
                successMessage?.let { message ->
                    MessageCard(
                        message = message,
                        type = MessageType.SUCCESS,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (showResendVerification && !isResetPasswordMode && !justResetPassword) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    OutlinedAppButton(
                        text = if (canResendEmail) {
                            stringResource(id = R.string.resend_verification_email)
                        } else {
                            "Resend in ${resendCooldownSeconds}s"
                        },
                        onClick = {
                            viewModel.clearError()
                            viewModel.resendVerificationEmail(email)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = canResendEmail,
                        loading = loading,
                        loadingText = stringResource(id = R.string.sending),
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.dont_have_account),
                color = AmericanBlue,
                fontFamily = Raleway,
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            OutlinedAppButton(
                text = stringResource(id = R.string.register),
                onClick = { navController.navigate(route = Register.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp),
                fontSize = 30.sp
            )
        }
    }
}