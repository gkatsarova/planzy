package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.data.SupabaseClient
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

    suspend fun signUp(email: String, password: String, username: String): Result<String> {
        return try {
            Log.d(TAG, "SIGNUP START")

            if (!isValidPassword(password)) {
                return Result.failure(Exception("Password must be at least 8 characters including lowercase, uppercase, numbers and special symbols."))
            }

            val usernameExists = checkUsernameExists(username)
            if (usernameExists) {
                Log.w(TAG, "Username already exists: $username")
                return Result.failure(Exception("This username already exists"))
            }

            if(!isValidUsername(username)){
                return Result.failure(Exception("Username must be 3-20 symbols, including lowercase, uppercase, numbers and special symbols(underscore, dot,)."))
            }

            val emailExists = checkEmailExistsInAuth(email)
            if (emailExists) {
                Log.w(TAG, "Email already exists in auth.users: $email")
                return Result.failure(Exception("This email is already registered. Please try logging in or use password reset."))
            }

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
                return Result.failure(Exception("Registration failed. Please try again."))
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
                    return Result.success("Registration successful! Welcome to Planzy.")
                } else {
                    Log.e(TAG, "Failed to create user record in database")
                    return Result.failure(Exception("Account created but profile setup failed. Please contact support."))
                }
            } else {
                Log.i(TAG, "Email verification is enabled - confirmation email sent")
                return Result.success("Confirmation email sent to $email. Please check your inbox and click the verification link.")
            }

        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed"))
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

    suspend fun checkUsernameExists(username: String): Boolean {
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
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
        return regex.matches(password)
    }

    fun isValidUsername(username: String): Boolean {
        val regex = Regex("^[a-z0-9._]{3,20}\$")
        return regex.matches(username)
    }
}