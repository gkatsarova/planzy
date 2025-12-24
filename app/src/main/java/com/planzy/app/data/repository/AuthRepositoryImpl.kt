package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.model.User
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProviderImpl
import com.planzy.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val resourceProvider: ResourceProviderImpl
) : AuthRepository {
    private val TAG = AuthRepositoryImpl::class.java.simpleName

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ): Result<UserInfo> {
        return try {
            Log.d(TAG, "Creating auth user...")

            val authResponse = SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("username", username)
                }
            }

            if (authResponse?.id == null) {
                Log.e(TAG, "Failed to create auth user - no ID returned")
                Result.failure(Exception(resourceProvider.getString(R.string.error_creating_auth_user)))
            } else {
                Log.i(TAG, "Auth user created with ID: ${authResponse.id}")
                Result.success(authResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in signUp: ${e.message}", e)
            Result.failure(handleGeneralException(e))
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<UserInfo> {
        return try {
            Log.d(TAG, "Signing in user...")

            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUser = SupabaseClient.client.auth.currentUserOrNull()

            if (currentUser == null) {
                Log.e(TAG, "Failed to get user after sign in")
                Result.failure(Exception(resourceProvider.getString(R.string.error_login_failed)))
            } else {
                Log.i(TAG, "Sign in successful for user: ${currentUser.id}")
                Result.success(currentUser)
            }
        } catch (e: AuthRestException) {
            Log.e(TAG, "Auth error in signIn: ${e.message}", e)
            Result.failure(handleAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG, "Error in signIn: ${e.message}", e)
            Result.failure(handleGeneralException(e))
        }
    }

    override suspend fun checkEmailExistsInAuth(email: String): Result<Boolean> {
        return try {
            val response = SupabaseClient.client.postgrest.rpc(
                function = "check_email_exists",
                parameters = mapOf("email_to_check" to email)
            )

            val exists = response.decodeAs<Boolean>()
            Log.d(TAG, "Email '$email' exists in auth.users: $exists")
            Result.success(exists)

        } catch (e: Exception) {
            Log.w(TAG, "Error checking email in auth.users: ${e.message}")
            Result.failure(handleGeneralException(e))
        }
    }

    override suspend fun checkUsernameExists(username: String): Result<Boolean> {
        return try {
            val response = SupabaseClient.client.postgrest["users"]
                .select {
                    filter {
                        eq("username", username)
                    }
                }

            val users = response.decodeList<User>()
            val exists = users.isNotEmpty()

            Log.d(TAG, "Username '$username' exists: $exists")
            Result.success(exists)

        } catch (e: Exception) {
            Log.w(TAG, "Error checking username: ${e.message}")
            Result.failure(handleGeneralException(e))
        }
    }

    override suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "Resending verification email to: $email")

            SupabaseClient.client.auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = email
            )

            Log.i(TAG, "Verification email resent successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to resend verification email: ${e.message}")
            Result.failure(handleGeneralException(e))
        }
    }

    override suspend fun getCurrentUser(): UserInfo? {
        return SupabaseClient.client.auth.currentUserOrNull()
    }

    private fun handleAuthException(e: AuthRestException): Exception {
        Log.e(TAG, "Handling auth exception: ${e.error}", e)

        val errorMessage = when {
            e.error.contains(resourceProvider.getString(R.string.auth_error_keyword_invalid_credentials), ignoreCase = true) -> {
                resourceProvider.getString(R.string.error_invalid_credentials)
            }
            e.error.contains(resourceProvider.getString(R.string.auth_error_keyword_email_not_confirmed), ignoreCase = true) ||
                    e.error.contains(resourceProvider.getString(R.string.auth_error_keyword_not_confirmed), ignoreCase = true) -> {
                resourceProvider.getString(R.string.error_email_not_verified)
            }
            else -> {
                e.message ?: resourceProvider.getString(R.string.error_auth_failed)
            }
        }

        return Exception(errorMessage)
    }

    private fun handleGeneralException(e: Exception): Exception {
        Log.e(TAG, "Error caught in repository: ${e.message}", e)

        return when {
            e is java.io.IOException || e.toString().contains(
                resourceProvider.getString(R.string.auth_error_keyword_unknown_exception),
                ignoreCase = true) -> {
                Exception(resourceProvider.getString(R.string.error_no_internet))
            }
            e.message.isNullOrBlank() -> {
                Exception(resourceProvider.getString(R.string.unknown_error))
            }
            else -> e
        }
    }
}