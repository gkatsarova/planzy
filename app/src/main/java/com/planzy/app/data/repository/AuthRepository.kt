package com.planzy.app.data.repository

import android.util.Log
import android.util.Patterns
import com.planzy.app.data.SupabaseClient
import com.planzy.app.data.repository.Messages.ERROR_EMAIL_INVALID
import com.planzy.app.data.repository.Messages.ERROR_PASSWORD_INVALID
import com.planzy.app.data.repository.Messages.SUCCESS_REGISTRATION
import com.planzy.app.data.repository.Messages.ERROR_EMAIL_EXISTS
import com.planzy.app.data.repository.Messages.ERROR_RECORD_DB_FAILED
import com.planzy.app.data.repository.Messages.ERROR_REGISTRATION_FAILED
import com.planzy.app.data.repository.Messages.ERROR_USERNAME_EXISTS
import com.planzy.app.data.repository.Messages.ERROR_USERNAME_INVALID
import com.planzy.app.data.repository.Messages.SUCCESS_VERIFICATION_EMAIL_SENT
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.delay


class AuthRepository {
    private val TAG = "AuthRepository"
    private val userRepo = UserRepository()

    companion object {
        private val USERNAME_REGEX = Regex("^[a-z0-9._]{3,20}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
    }

    suspend fun signUp(email: String, password: String, username: String): Result<String> {
        return try {
            Log.d(TAG, "SIGNUP START")

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
                return Result.failure(Exception(ERROR_REGISTRATION_FAILED))
            }

            Log.i(TAG, "Auth user created with ID: ${authResponse.id}")

            delay(1000)

            val currentUser = SupabaseClient.client.auth.currentUserOrNull()

            if (currentUser != null) {
                Log.i(TAG, "User is logged in automatically (email verification disabled)")
                Log.d(TAG, "Current user ID: ${currentUser.id}")

                val created = userRepo.createUserRecord(
                    authId = authResponse.id,
                    email = email,
                    username = username
                )

                if (created) {
                    Log.i(TAG, "Registration complete! User record created.")
                    return Result.success(SUCCESS_REGISTRATION)
                } else {
                    Log.e(TAG, "Failed to create user record in database")
                    return Result.failure(Exception(ERROR_RECORD_DB_FAILED))
                }
            } else {
                Log.i(TAG, "Email verification is enabled - confirmation email sent")
                return Result.success(SUCCESS_VERIFICATION_EMAIL_SENT)
            }

        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: ERROR_REGISTRATION_FAILED))
        }
    }

    private suspend fun checkEmailExistsInAuth(email: String): Boolean {
        return try {
            val response = SupabaseClient.client.postgrest.rpc(
                function = "check_email_exists",
                parameters = mapOf("email_to_check" to email)
            )

            val exists = response.decodeAs<Boolean>()
            Log.d(TAG, "Email '$email' exists in auth.users: $exists")
            exists

        } catch (e: Exception) {
            Log.w(TAG, "Error checking email in auth.users: ${e.message}")
            false
        }
    }

    private suspend fun checkUsernameExists(username: String): Boolean {
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
            exists

        } catch (e: Exception) {
            Log.w(TAG, "Error checking username: ${e.message}")
            false
        }
    }

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }

    fun isValidUsername(username: String): Boolean {
        return USERNAME_REGEX.matches(username)
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun getPasswordValidationError(password: String): String? {
        return if (password.isEmpty() || isValidPassword(password)) null else ERROR_PASSWORD_INVALID
    }

    fun getUsernameValidationError(username: String): String? {
        return if (username.isEmpty() || isValidUsername(username)) null else ERROR_USERNAME_INVALID
    }

    fun getEmailValidationError(email: String): String? {
        return if (email.isEmpty() || isValidEmail(email)) null else ERROR_EMAIL_INVALID
    }

    suspend fun getUsernameExistsError(username: String): String? {
        return if (checkUsernameExists(username)) ERROR_USERNAME_EXISTS else null
    }

    suspend fun getEmailExistsError(email: String): String? {
        return if (checkEmailExistsInAuth(email)) ERROR_EMAIL_EXISTS else null
    }
}