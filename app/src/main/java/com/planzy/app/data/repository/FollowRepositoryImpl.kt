package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.model.Follow
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.FollowStats
import com.planzy.app.domain.repository.FollowRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class FollowRepositoryImpl(
    private val resourceProvider: ResourceProvider
) : FollowRepository {
    private val TAG = FollowRepositoryImpl::class.java.simpleName

    override suspend fun followUser(followingId: String): Result<Unit> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val followerId = currentUser.id

            Log.d(TAG, "Following user: $followingId by user: $followerId")

            SupabaseClient.client.postgrest["follows"].insert(
                mapOf(
                    "follower_id" to followerId,
                    "following_id" to followingId
                )
            )

            Log.i(TAG, "Successfully followed user: $followingId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error following user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(followingId: String): Result<Unit> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val followerId = currentUser.id

            Log.d(TAG, "Unfollowing user: $followingId by user: $followerId")

            SupabaseClient.client.postgrest["follows"].delete {
                filter {
                    eq("follower_id", followerId)
                    eq("following_id", followingId)
                }
            }

            Log.i(TAG, "Successfully unfollowed user: $followingId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error unfollowing user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getFollowStats(userId: String): Result<FollowStats> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            val currentUserId = currentUser?.id

            Log.d(TAG, "Getting follow stats for user: $userId, current user: $currentUserId")

            val response = SupabaseClient.client.postgrest["users"]
                .select {
                    filter {
                        eq("auth_id", userId)
                    }
                }

            val users = response.decodeList<com.planzy.app.data.model.User>()
            val user = users.firstOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.user_not_found)))

            val followersCount = user.followersCount
            val followingCount = user.followingCount

            Log.d(TAG, "User found with followers: $followersCount, following: $followingCount")

            var isFollowing = false
            if (currentUserId != null && currentUserId.isNotEmpty() && currentUserId != "null") {
                try {
                    val isFollowingResponse = SupabaseClient.client.postgrest["follows"]
                        .select {
                            filter {
                                eq("follower_id", currentUserId)
                                eq("following_id", userId)
                            }
                        }

                    val follows = isFollowingResponse.decodeList<Follow>()
                    isFollowing = follows.isNotEmpty()
                    Log.d(TAG, "Is following check: $isFollowing")
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking if following: ${e.message}")
                }
            }

            Log.d(TAG, "Follow stats for user $userId: followers=$followersCount, following=$followingCount, isFollowing=$isFollowing")

            Result.success(
                FollowStats(
                    followersCount = followersCount,
                    followingCount = followingCount,
                    isFollowing = isFollowing
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting follow stats: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun isFollowing(userId: String): Result<Boolean> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val currentUserId = currentUser.id

            val response = SupabaseClient.client.postgrest["follows"]
                .select {
                    filter {
                        eq("follower_id", currentUserId)
                        eq("following_id", userId)
                    }
                }

            val follows = response.decodeList<Follow>()
            Result.success(follows.isNotEmpty())

        } catch (e: Exception) {
            Log.e(TAG, "Error checking if following: ${e.message}", e)
            Result.failure(e)
        }
    }
}