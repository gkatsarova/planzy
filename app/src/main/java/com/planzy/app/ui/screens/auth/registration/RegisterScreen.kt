package com.planzy.app.ui.screens.auth.registration

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
import com.planzy.app.data.repository.UserRepositoryImpl
import com.planzy.app.R
import com.planzy.app.data.util.CooldownManager
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.screens.components.AuthButton
import com.planzy.app.ui.screens.components.InputTextField
import com.planzy.app.ui.screens.components.OutlinedAppButton
import com.planzy.app.ui.screens.components.PasswordTextField
import com.planzy.app.ui.theme.AmericanBlue
import com.planzy.app.ui.theme.ErrorColor
import com.planzy.app.ui.theme.Raleway
import com.planzy.app.ui.screens.components.MessageCard
import com.planzy.app.ui.screens.components.MessageType

@Composable
fun RegisterScreen(
    navController: NavController,
    deepLinkViewModel: DeepLinkViewModel
) {
    val context = LocalContext.current
    val resourceProvider = remember { ResourceProviderImpl(context = context) }
    val authRepo = remember { AuthRepositoryImpl(resourceProvider = ResourceProviderImpl(context)) }
    val userRepo = remember { UserRepositoryImpl() }
    val cooldownManager = remember { CooldownManager(context) }

    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModel.Factory(
            resourceProvider = resourceProvider,
            authRepository = authRepo,
            userRepository = userRepo,
            cooldownManager = cooldownManager
        )
    )

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val canResendEmail by viewModel.canResendEmail.collectAsState()
    val resendCooldownSeconds by viewModel.resendCooldownSeconds.collectAsState()

    LaunchedEffect(success) {
        if (success) {
            deepLinkViewModel.savePendingCredentials(email, password)
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
                text = stringResource(id = R.string.register_screen),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 38.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            InputTextField(
                value = username,
                label = stringResource(id = R.string.username),
                onValueChange = {
                    username = it
                    viewModel.validateUsername(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(70.dp),
                isError = fieldErrors.usernameError != null
            )
            fieldErrors.usernameError?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp, end = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        .padding(start = 24.dp, top = 4.dp, end = 24.dp)
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
                        .padding(start = 24.dp, top = 4.dp, end = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AuthButton(
                text = stringResource(id = R.string.register),
                onClick = {
                    viewModel.clearError()
                    viewModel.clearSuccess()
                    viewModel.signUp(email, password, username)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp),
                enabled = !loading &&
                        email.isNotBlank() &&
                        username.isNotBlank() &&
                        password.isNotBlank() &&
                        fieldErrors.usernameError == null &&
                        fieldErrors.emailError == null &&
                        fieldErrors.passwordError == null,
                loading = loading,
                loadingText = stringResource(id = R.string.registering)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        MessageCard(
                            message = message,
                            type = MessageType.SUCCESS,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (message.contains(
                                stringResource(id = R.string.verification_email),
                                ignoreCase = true
                            )
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))

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
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.already_have_account),
                color = AmericanBlue,
                fontFamily = Raleway,
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            OutlinedAppButton(
                text = stringResource(R.string.login),
                onClick = { navController.navigate(route = Login.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp),
                fontSize = 30.sp
            )
        }
    }
}