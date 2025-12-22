package com.planzy.app.data.repository

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import com.planzy.app.data.util.ResourceProviderImpl

class DeepLinkHandler(
    private val resourceProvider: ResourceProviderImpl
) {
    private val TAG = DeepLinkHandler::class.java.simpleName
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
            DeepLinkResult.Error(e.message ?: resourceProvider.getString(R.string.unknown_error))
        }
    }

    private suspend fun handleAuthCallback(uri: Uri): DeepLinkResult {
        val type = uri.getQueryParameter("type")
        val accessToken = uri.getQueryParameter("access_token")

        return when (type) {
            "signup", "email_confirmation" -> {
                Log.d(TAG, "Email verification deep link received")
                delay(2000)
                handleEmailVerification()
            }
            "recovery" -> {
                DeepLinkResult.PasswordRecovery(accessToken)
            }
            else -> DeepLinkResult.Unknown
        }
    }

    private suspend fun handleEmailVerification(): DeepLinkResult {
        return try {
            Log.d(TAG, "Processing email verification...")

            delay(1500)

            val user = SupabaseClient.client.auth.currentUserOrNull()

            if (user == null) {
                Log.w(TAG, "No current user after verification")
                return DeepLinkResult.Error(resourceProvider.getString(R.string.error_active_session))
            }

            Log.d(TAG, "User verified: ${user.email}")

            val email = user.email
                ?: return DeepLinkResult.Error(resourceProvider.getString(R.string.error_verified_user_email))

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
                Log.w(TAG, "Failed to create user record")
                DeepLinkResult.Error(resourceProvider.getString(R.string.error_record_db_failed))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during email verification: ${e.message}", e)
            DeepLinkResult.Error(e.message ?: resourceProvider.getString(R.string.error_email_verification))
        }
    }
}

sealed class DeepLinkResult {
    object NoDeepLink : DeepLinkResult()
    object Unknown : DeepLinkResult()
    data class EmailVerified(val email: String) : DeepLinkResult()
    data class PasswordRecovery(val token: String?) : DeepLinkResult()
    data class Error(val message: String) : DeepLinkResult()
}