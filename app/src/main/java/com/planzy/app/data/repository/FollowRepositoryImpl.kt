package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.model.Follow
import com.planzy.app.data.model.User
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.SupabaseClient.currentUserIdFlow
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.FollowStats
import com.planzy.app.domain.repository.FollowRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class FollowRepositoryImpl(
    private val resourceProvider: ResourceProvider
) : FollowRepository {
    private val TAG = FollowRepositoryImpl::class.java.simpleName

    override suspend fun followUser(followingId: String): Result<Unit> {
        return try {
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val followerId = currentUserId

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
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val followerId = currentUserId

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
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            Log.d(TAG, "Getting follow stats for user: $userId, current user: $currentUserId")

            val response = SupabaseClient.client.postgrest["users"]
                .select {
                    filter {
                        eq("auth_id", userId)
                    }
                }

            val users = response.decodeList<User>()
            val user = users.firstOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.user_not_found)))

            val followersCount = user.followersCount
            val followingCount = user.followingCount

            Log.d(TAG, "User found with followers: $followersCount, following: $followingCount")

            var isFollowing = false
            if (currentUserId.isNotEmpty() && currentUserId != "null") {
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
                    isFollowing = false
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
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

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

    override suspend fun getFollowers(userId: String): Result<List<User>> {
        return try {
            Log.d(TAG, "Getting followers for user: $userId")

            val followsResponse = SupabaseClient.client.postgrest["follows"]
                .select {
                    filter {
                        eq("following_id", userId)
                    }
                }

            val follows = followsResponse.decodeList<Follow>()
            val followerIds = follows.map { it.follower_id }

            if (followerIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val followers = mutableListOf<User>()
            for (followerId in followerIds) {
                try {
                    val userResponse = SupabaseClient.client.postgrest["users"]
                        .select {
                            filter {
                                eq("auth_id", followerId)
                            }
                        }

                    val users = userResponse.decodeList<User>()
                    users.firstOrNull()?.let { followers.add(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching follower info for $followerId: ${e.message}")
                }
            }

            Log.d(TAG, "Found ${followers.size} followers for user $userId")
            Result.success(followers)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting followers: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getFollowing(userId: String): Result<List<User>> {
        return try {
            Log.d(TAG, "Getting following for user: $userId")

            val followsResponse = SupabaseClient.client.postgrest["follows"]
                .select {
                    filter {
                        eq("follower_id", userId)
                    }
                }

            val follows = followsResponse.decodeList<Follow>()
            val followingIds = follows.map { it.following_id }

            if (followingIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val following = mutableListOf<User>()
            for (followingId in followingIds) {
                try {
                    val userResponse = SupabaseClient.client.postgrest["users"]
                        .select {
                            filter {
                                eq("auth_id", followingId)
                            }
                        }

                    val users = userResponse.decodeList<User>()
                    users.firstOrNull()?.let { following.add(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching following info for $followingId: ${e.message}")
                }
            }

            Log.d(TAG, "Found ${following.size} following for user $userId")
            Result.success(following)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting following: ${e.message}", e)
            Result.failure(e)
        }
    }
}