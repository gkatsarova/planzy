package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.data.model.User
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.postgrest

class UserRepositoryImpl : UserRepository {
    private val TAG = "UserRepositoryImpl"

    override suspend fun createUserRecord(
        authId: String,
        email: String,
        username: String
    ): Result<Unit> {
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
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating user record: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserByUsername(username: String): Result<User?> {
        return try {
            val response = SupabaseClient.client.postgrest["users"]
                .select {
                    filter {
                        eq("username", username)
                    }
                }

            val users = response.decodeList<User>()
            Result.success(users.firstOrNull())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by username: ${e.message}", e)
            Result.failure(e)
        }
    }
}