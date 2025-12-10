package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val auth_id: String,
    val username: String,
    val email: String
)

class UserRepository {
    private val TAG = "UserRepository"
    suspend fun createUserRecord(
        authId: String,
        email: String,
        username: String
    ): Boolean {
        return try {
            Log.d(TAG, "Creating user record in database...")
            Log.d(TAG, "  Auth ID: $authId")
            Log.d(TAG, "  Email: $email")
            Log.d(TAG, "  Username: $username")

            SupabaseClient.client.postgrest["users"].upsert(
                mapOf(
                    "auth_id" to authId,
                    "email" to email,
                    "username" to username
                )
            )

            Log.i(TAG, "User record created successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error creating user record: ${e.message}", e)
            false
        }
    }
}