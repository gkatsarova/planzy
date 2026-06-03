package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.data.model.UserCommentDTO
import com.planzy.app.data.model.UserCommentInsertDTO
import com.planzy.app.data.model.UserCommentStatsDTO
import com.planzy.app.data.model.toDTO
import com.planzy.app.data.model.toDomainModel
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.SupabaseClient.currentUserIdFlow
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.domain.model.AppError
import com.planzy.app.domain.model.AppException
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.PlaceReview
import com.planzy.app.domain.model.UserComment
import com.planzy.app.domain.repository.PlacesRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException

class PlacesRepositoryImpl(
    private val tripadvisorApi: TripadvisorApi,
    private val supabaseClient: SupabaseClient
) : PlacesRepository {
    private val TAG = PlacesRepositoryImpl::class.java.simpleName

    override suspend fun searchPlaces(
        query: String,
        minRating: Double,
        latLong: String?,
        radius: Int?
    ): Result<List<Place>> {
        return try {
            val response = tripadvisorApi.searchLocations(
                query = query,
                latLong = latLong,
                radius = radius
            ).getOrThrow()

            val results = response.data ?: emptyList()

            val places = results.mapNotNull { item ->
                val locationId = item.locationId
                if (locationId.isBlank()) return@mapNotNull null

                val detailsResult = tripadvisorApi.getLocationDetails(locationId).getOrThrow()
                var domainPlace = detailsResult.toDomainModel()

                if (domainPlace.photoUrl.isNullOrEmpty()) {
                    val photosResult = tripadvisorApi.getLocationPhotos(locationId).getOrNull()
                    val photoUrl = photosResult?.data?.firstOrNull()?.images?.large?.url
                    if (photoUrl != null) {
                        domainPlace = domainPlace.copy(photoUrl = photoUrl)
                    }
                }
                domainPlace
            }

            Result.success(places)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlaceDetails(locationId: String): Result<Place> {
        return try {
            val detailsResult = tripadvisorApi.getLocationDetails(locationId).getOrThrow()
            var domainPlace = detailsResult.toDomainModel()

            if (domainPlace.photoUrl.isNullOrEmpty()) {
                val photosResult = tripadvisorApi.getLocationPhotos(locationId).getOrNull()
                val photoUrl = photosResult?.data?.firstOrNull()?.images?.large?.url
                    ?: photosResult?.data?.firstOrNull()?.images?.medium?.url

                if (photoUrl != null) {
                    domainPlace = domainPlace.copy(photoUrl = photoUrl)
                }
            }

            Result.success(domainPlace)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlacePhotos(locationId: String): Result<List<String>> =
        tripadvisorApi.getLocationPhotos(locationId).map { response ->
            response.data?.mapNotNull { it.images?.large?.url } ?: emptyList()
        }

    override suspend fun getPlaceReviews(locationId: String, limit: Int): Result<List<PlaceReview>> {
        return try {
            val reviewsResponse = tripadvisorApi.getLocationReviews(locationId, limit).getOrThrow()
            val reviews = reviewsResponse.data?.map { it.toDomainModel() } ?: emptyList()
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserComments(placeId: String): Result<List<UserComment>> {
        return try {
            if (placeId.isBlank()) {
                return Result.failure(AppException(AppError.ERROR_EMPTY_PLACE_ID))
            }

            val response = supabaseClient.client.postgrest
                .from("user_comments")
                .select(Columns.raw("*, users(username)")) {
                    filter {
                        eq("place_id", placeId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }

            val comments = response.decodeList<UserCommentDTO>().map { dto ->
                dto.toDomainModel(
                    currentUserId = withTimeoutOrNull(2000L) {
                    currentUserIdFlow.filterNotNull().first()
                    }
                )
            }

            Result.success(comments)
        } catch (e: SerializationException) {
            Log.e(TAG, "Serialization error: ${e.message}", e)
            Result.failure(AppException(AppError.ERROR_PARSING_COMMENTS))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting comments: ${e.message}", e)
            Result.failure(AppException(AppError.UNKNOWN_ERROR))
        }
    }

    override suspend fun addUserComment(
        placeId: String,
        text: String,
        rating: Int
    ): Result<UserComment> {
        return try {
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(AppException(AppError.USER_NOT_LOGGED_IN))

            val commentToInsert = UserCommentInsertDTO(
                placeId = placeId,
                userId = currentUserId,
                text = text.trim(),
                rating = rating
            )

            val response = supabaseClient.client.postgrest
                .from("user_comments")
                .insert(commentToInsert) {
                    select(Columns.raw("*, users(username)"))
                }

            val comment = response.decodeSingle<UserCommentDTO>().toDomainModel(
                currentUserId = currentUserId
            )

            Result.success(comment)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment: ${e.message}", e)
            Result.failure(AppException(AppError.ERROR_POSTING_COMMENT))
        }
    }

    override suspend fun updateUserComment(
        commentId: String,
        text: String,
        rating: Int
    ): Result<Unit> {
        return try {
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(AppException(AppError.USER_NOT_LOGGED_IN))

            supabaseClient.client.postgrest
                .from("user_comments")
                .update({
                    set("text", text.trim())
                    set("rating", rating)
                }) {
                    filter {
                        eq("id", commentId)
                        eq("user_id", currentUserId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating comment: ${e.message}", e)
            Result.failure(AppException(AppError.ERROR_UPDATING_COMMENT))
        }
    }

    override suspend fun deleteUserComment(commentId: String): Result<Unit> {
        return try {
            val currentUserId = withTimeoutOrNull(1500L) {
                currentUserIdFlow
                    .filterNotNull()
                    .first()
            } ?: return Result.failure(AppException(AppError.USER_NOT_LOGGED_IN))

            supabaseClient.client.postgrest
                .from("user_comments")
                .delete {
                    filter {
                        eq("id", commentId)
                        eq("user_id", currentUserId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment: ${e.message}", e)
            Result.failure(AppException(AppError.ERROR_DELETING_COMMENT))
        }
    }

    override suspend fun getUserCommentsStats(placeId: String): Result<Pair<Double?, Int>> {
        return try {
            if (placeId.isBlank()) {
                return Result.success(Pair(null, 0))
            }

            val response = supabaseClient.client.postgrest
                .from("user_comments")
                .select(Columns.raw("rating")) {
                    filter {
                        eq("place_id", placeId)
                    }
                }

            val ratings = response.decodeList<UserCommentStatsDTO>()

            if (ratings.isEmpty()) {
                Result.success(Pair(null, 0))
            } else {
                val averageRating = ratings.map { it.rating.toDouble() }.average()
                Result.success(Pair(averageRating, ratings.size))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting comments stats: ${e.message}", e)
            Result.success(Pair(null, 0))
        }
    }

    override suspend fun savePlace(place: Place): Result<Unit> {
        return try {
            Log.d(TAG, "Saving place to database: ${place.name} (${place.id})")
            val placeDTO = place.toDTO()

           supabaseClient.client.postgrest["places"]
                .upsert(placeDTO) {
                    onConflict = "location_id"
                }

            Log.d(TAG, "Successfully saved place: ${place.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving place: ${e.message}", e)
            Result.failure(AppException(AppError.ERROR_SAVING_PLACE))
        }
    }
}