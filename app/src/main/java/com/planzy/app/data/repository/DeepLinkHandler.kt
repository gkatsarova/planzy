package com.planzy.app.data.repository

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.RecoverySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseFragmentAndImportSession
import kotlinx.coroutines.delay
import com.planzy.app.domain.model.AppError
import io.github.jan.supabase.annotations.SupabaseInternal

class DeepLinkHandler(
    private val recoverySessionManager: RecoverySessionManager
) {
    private val TAG = DeepLinkHandler::class.java.simpleName

    companion object {
        private const val DUPLICATE = "duplicate"
        private const val ALREADY_EXISTS = "already exists"
        private const val UNIQUE = "unique"
    }
    private val userRepo = UserRepositoryImpl()

    suspend fun handleAuthDeepLink(intent: Intent?): DeepLinkResult {
        val uri = intent?.data ?: return DeepLinkResult.NoDeepLink

        return try {
            when {
                uri.scheme == "planzy" && uri.host == "auth-callback" -> {
                    handleAuthCallback(uri)
                }
                else -> DeepLinkResult.Unknown
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling deep link: ${e.message}", e)
            DeepLinkResult.Error(AppError.UNKNOWN_ERROR)
        }
    }

    @OptIn(SupabaseInternal::class)
    private suspend fun handleAuthCallback(uri: Uri): DeepLinkResult {
        val fragment = uri.fragment
        Log.d(TAG, "URI fragment: $fragment")

        if (fragment.isNullOrEmpty()) {
            Log.w(TAG, "No fragment in URI")
            return DeepLinkResult.Unknown
        }

        val params = fragment.split("&").associate { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                parts[0] to parts[1]
            } else {
                parts[0] to ""
            }
        }

        val type = params["type"]
        val accessToken = params["access_token"]
        val refreshToken = params["refresh_token"]

        return when (type) {
            "signup", "email_confirmation" -> {
                Log.d(TAG, "Email verification deep link received")

                try {
                    SupabaseClient.client.auth.parseFragmentAndImportSession(uri.toString())

                    delay(500)

                    handleEmailVerification()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import session from fragment: ${e.message}", e)
                    val email = uri.getQueryParameter("email") ?: params["email"]
                    DeepLinkResult.Error(AppError.ERROR_EMAIL_VERIFICATION, email)
                }
            }
            "recovery" -> {
                Log.d(TAG, "Password recovery deep link received")
                try {
                    if (accessToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
                        Log.e(TAG, "Missing tokens in recovery link")
                        return DeepLinkResult.Error(AppError.ERROR_INVALID_RESET_LINK)
                    }
                    recoverySessionManager.saveRecoverySession(accessToken, refreshToken)

                    Log.d(TAG, "Parsing recovery fragment and importing session...")

                    SupabaseClient.client.auth.parseFragmentAndImportSession(uri.toString())

                    delay(500)

                    DeepLinkResult.PasswordReset
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import recovery session: ${e.message}", e)
                    if (recoverySessionManager.getRecoverySession() != null) {
                        Log.d(TAG, "Recovery tokens saved locally, proceeding with password reset")
                        DeepLinkResult.PasswordReset
                    } else {
                        DeepLinkResult.Error(AppError.ERROR_SESSION)
                    }
                }
            }
            else -> DeepLinkResult.Unknown
        }
    }

    private suspend fun handleEmailVerification(): DeepLinkResult {
        return try {
            Log.d(TAG, "Processing email verification...")

            val user = SupabaseClient.client.auth.currentUserOrNull()

            if (user == null) {
                Log.w(TAG, "No current user after verification")
                return DeepLinkResult.Error(AppError.ERROR_ACTIVE_SESSION)
            }

            Log.d(TAG, "User verified: ${user.email}")

            val email = user.email
                ?: return DeepLinkResult.Error(AppError.ERROR_VERIFIED_USER_EMAIL)

            val username = user.userMetadata?.get("username")?.toString()
                ?: user.email?.substringBefore("@")
                ?: "user"

            Log.d(TAG, "Creating user record with username: $username")

            val result = userRepo.createUserRecord(
                authId = user.id,
                email = email,
                username = username
            )

            if (result.isSuccess) {
                Log.i(TAG, "Email verified and user record created")
                DeepLinkResult.EmailVerified(email)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                Log.e(TAG, "Failed to create user record")

                if (errorMsg?.contains(DUPLICATE, ignoreCase = true) == true ||
                    errorMsg?.contains(ALREADY_EXISTS, ignoreCase = true) == true ||
                    errorMsg?.contains(UNIQUE, ignoreCase = true) == true) {
                    DeepLinkResult.EmailVerified(email)
                } else {
                    DeepLinkResult.Error(AppError.ERROR_RECORD_DB_FAILED)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during email verification: ${e.message}", e)
            DeepLinkResult.Error(AppError.ERROR_EMAIL_VERIFICATION)
        }
    }
}

sealed class DeepLinkResult {
    object NoDeepLink : DeepLinkResult()
    object Unknown : DeepLinkResult()
    data class EmailVerified(val email: String) : DeepLinkResult()
    object PasswordReset : DeepLinkResult()
    data class Error(val error: AppError, val emailArg: String? = null) : DeepLinkResult()
}