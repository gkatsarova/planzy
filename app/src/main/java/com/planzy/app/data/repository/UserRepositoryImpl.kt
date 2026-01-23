package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.data.model.User
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.repository.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.io.File
import com.planzy.app.R

class UserRepositoryImpl(
    private val resourceProvider: ResourceProvider
) : UserRepository {
    private val TAG = UserRepositoryImpl::class.java.simpleName
    private val BUCKET_NAME = "profile-picture"

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

    override suspend fun getUserByAuthId(authId: String): Result<User?> {
        return try {
            val response = SupabaseClient.client.postgrest["users"]
                .select {
                    filter {
                        eq("auth_id", authId)
                    }
                }

            val users = response.decodeList<User>()
            Result.success(users.firstOrNull())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by auth_id: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePicture(imageFile: File): Result<String> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val authId = currentUser.id

            Log.d(TAG, "Uploading profile picture for user: $authId")

            val fileName = "$authId/${System.currentTimeMillis()}.jpg"

            SupabaseClient.client.storage
                .from(BUCKET_NAME)
                .upload(fileName, imageFile.readBytes())

            val publicUrl = SupabaseClient.client.storage
                .from(BUCKET_NAME)
                .publicUrl(fileName)

            Log.i(TAG, "Profile picture uploaded: $publicUrl")
            Result.success(publicUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile picture: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePictureUrl(url: String): Result<Unit> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val authId = currentUser.id

            Log.d(TAG, "Updating profile picture URL for user: $authId")

            SupabaseClient.client.postgrest["users"]
                .update({
                    set("profile_picture_url", url)
                }) {
                    filter {
                        eq("auth_id", authId)
                    }
                }

            Log.i(TAG, "Profile picture URL updated successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile picture URL: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProfilePicture(pictureUrl: String): Result<Unit> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val authId = currentUser.id

            Log.d(TAG, "Deleting profile picture for user: $authId")

            val fileName = pictureUrl.substringAfter("$BUCKET_NAME/")

            SupabaseClient.client.storage
                .from(BUCKET_NAME)
                .delete(fileName)

            val nullUrl: String? = null
            SupabaseClient.client.postgrest["users"]
                .update({
                    set("profile_picture_url", nullUrl)
                }) {
                    filter {
                        eq("auth_id", authId)
                    }
                }

            Log.i(TAG, "Profile picture deleted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile picture: ${e.message}", e)
            Result.failure(e)
        }
    }
}