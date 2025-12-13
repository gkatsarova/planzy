package com.planzy.app.ui.screens.registration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.planzy.app.data.repository.AuthRepository
import com.planzy.app.R
import com.planzy.app.ui.navigation.Login
import com.planzy.app.ui.screens.components.InputTextField

@Composable
fun RegisterScreen(navController: NavController) {
    val authRepo = remember { AuthRepository() }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(authRepo)
    )

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loading by authViewModel.loading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val success by authViewModel.success.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()
    val fieldErrors by authViewModel.fieldErrors.collectAsState()
    val canResendEmail by authViewModel.canResendEmail.collectAsState()
    val resendCooldownSeconds by authViewModel.resendCooldownSeconds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                authViewModel.validateUsername(it)
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = VisualTransformation.None,
            isError = fieldErrors.usernameError != null
        )
        fieldErrors.usernameError?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InputTextField(
            value = email,
            label = stringResource(id = R.string.email),
            onValueChange = {
                email = it
                authViewModel.validateEmail(it)
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = VisualTransformation.None,
            isError = fieldErrors.emailError != null
        )
        fieldErrors.emailError?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InputTextField(
            value = password,
            label = stringResource(id = R.string.password),
            onValueChange = {
                password = it
                authViewModel.validatePassword(it)
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = fieldErrors.passwordError != null
        )
        fieldErrors.passwordError?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                authViewModel.clearError()
                authViewModel.clearSuccess()
                authViewModel.signUp(email, password, username)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading &&
                    email.isNotBlank() &&
                    username.isNotBlank() &&
                    password.isNotBlank() &&
                    fieldErrors.usernameError == null &&
                    fieldErrors.emailError == null &&
                    fieldErrors.passwordError == null
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registration in progress...")
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Already have an account? Login"
        )

        OutlinedButton(
            onClick = { navController.navigate(route = Login.route) },
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (success && successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (successMessage!!.contains("Verification email", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                authViewModel.clearError()
                                authViewModel.resendVerificationEmail(email)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading && canResendEmail
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending...")
                            } else {
                                Text(
                                    if (canResendEmail) {
                                        "Resend Verification Email"
                                    } else {
                                        "Resend in ${resendCooldownSeconds}s"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}