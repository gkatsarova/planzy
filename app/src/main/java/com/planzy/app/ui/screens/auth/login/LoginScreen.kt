package com.planzy.app.ui.screens.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.planzy.app.data.repository.AuthRepositoryImpl
import com.planzy.app.R
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.screens.components.AuthButton
import com.planzy.app.ui.screens.components.InputTextField
import com.planzy.app.ui.screens.components.OutlinedAppButton
import com.planzy.app.ui.screens.components.PasswordTextField
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.screens.components.MessageCard
import com.planzy.app.ui.screens.components.MessageType

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context = context) }
    val authRepo = remember { AuthRepositoryImpl(resourceProvider = ResourceProviderImpl(context)) }

    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(
            authRepository = authRepo,
            resourceProvider = resourceProvider
        )
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val showResendVerification by viewModel.showResendVerification.collectAsState()
    val canResendEmail by viewModel.canResendEmail.collectAsState()
    val resendCooldownSeconds by viewModel.resendCooldownSeconds.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Login",
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

            Spacer(modifier = Modifier.height(24.dp))

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
                loadingText = "Logging in"
            )

            Spacer(modifier = Modifier.height(16.dp))

            error?.let { errorMessage ->
                MessageCard(
                    message = errorMessage,
                    type = MessageType.ERROR,
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

            if (showResendVerification) {
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
        }
    }
}