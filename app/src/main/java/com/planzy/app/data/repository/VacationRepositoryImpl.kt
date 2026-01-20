package com.planzy.app.data.repository

import android.util.Log
import com.planzy.app.R
import com.planzy.app.data.model.*
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.model.VacationPlace
import com.planzy.app.domain.repository.VacationsRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import com.planzy.app.domain.model.VacationComment
import io.github.jan.supabase.postgrest.from

@Serializable
data class UserInfo(
    val username: String
)

class VacationsRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val resourceProvider: ResourceProvider
) : VacationsRepository {

    private val TAG = VacationsRepositoryImpl::class.java.simpleName

    override suspend fun getUserVacations(): Result<List<Vacation>> {
        return try {
            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val vacationsResponse = supabaseClient.client.postgrest
                .from("vacations")
                .select {
                    filter {
                        eq("user_id", currentUserId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }

            val vacationDTOs = vacationsResponse.decodeList<VacationDTO>()

            val vacationsWithCount = vacationDTOs.map { vacationDTO ->
                val placesCount = try {
                    val placesResponse = supabaseClient.client.postgrest
                        .from("vacation_places")
                        .select(Columns.raw("id")) {
                            filter {
                                eq("vacation_id", vacationDTO.id)
                            }
                        }
                    placesResponse.decodeList<VacationIdDTO>().size
                } catch (_: Exception) {
                    0
                }

                Vacation(
                    id = vacationDTO.id,
                    userId = vacationDTO.userId,
                    title = vacationDTO.title,
                    createdAt = vacationDTO.createdAt,
                    placesCount = placesCount
                )
            }

            Result.success(vacationsWithCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vacations: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_loading_vacations)))
        }
    }

    override suspend fun createVacation(title: String): Result<Vacation> {
        return try {
            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            if (title.isBlank()) {
                return Result.failure(Exception(resourceProvider.getString(R.string.error_empty_vacation_title)))
            }

            val vacationToInsert = VacationInsertDTO(
                userId = currentUserId,
                title = title.trim()
            )

            val response = supabaseClient.client.postgrest
                .from("vacations")
                .insert(vacationToInsert) {
                    select()
                }

            val vacation = response.decodeSingle<VacationDTO>().toDomainModel()
            Result.success(vacation)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating vacation: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_creating_vacation)))
        }
    }

    override suspend fun addPlaceToVacation(vacationId: String, placeId: String): Result<VacationPlace> {
        return try {
            Log.d(TAG, "Adding place $placeId to vacation $vacationId")

            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
            if (currentUserId == null) {
                Log.e(TAG, "User not logged in")
                return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))
            }

            val vacationCheck = supabaseClient.client.postgrest
                .from("vacations")
                .select(Columns.raw("id, user_id")) {
                    filter {
                        eq("id", vacationId)
                    }
                    limit(1)
                }

            val vacation = try {
                vacationCheck.decodeList<VacationIdDTO>().firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Vacation not found: ${e.message}")
                null
            }

            if (vacation == null) {
                Log.e(TAG, "Vacation $vacationId does not exist")
                return Result.failure(Exception(resourceProvider.getString(R.string.error_vacation_not_found)))
            }

            val existingPlaceCheck = supabaseClient.client.postgrest
                .from("vacation_places")
                .select(Columns.raw("id")) {
                    filter {
                        eq("vacation_id", vacationId)
                        eq("place_id", placeId)
                    }
                    limit(1)
                }

            val placeAlreadyExists = try {
                existingPlaceCheck.decodeList<VacationIdDTO>().isNotEmpty()
            } catch (_: Exception) {
                false
            }

            if (placeAlreadyExists) {
                Log.w(TAG, "Place already exists in vacation")
                return Result.failure(Exception(resourceProvider.getString(R.string.error_place_already_in_vacation)))
            }

            val existingPlaces = supabaseClient.client.postgrest
                .from("vacation_places")
                .select(Columns.raw("order_index")) {
                    filter {
                        eq("vacation_id", vacationId)
                    }
                    order("order_index", order = Order.DESCENDING)
                    limit(1)
                }

            val maxOrderIndex = try {
                existingPlaces.decodeList<OrderIndexDTO>()
                    .firstOrNull()?.orderIndex ?: 0
            } catch (_: Exception) {
                0
            }

            Log.d(TAG, "Max order index: $maxOrderIndex, new will be: ${maxOrderIndex + 1}")

            val placeToInsert = VacationPlaceInsertDTO(
                vacationId = vacationId,
                placeId = placeId,
                orderIndex = maxOrderIndex + 1
            )

            val response = supabaseClient.client.postgrest
                .from("vacation_places")
                .insert(placeToInsert) {
                    select()
                }

            val vacationPlace = response.decodeSingle<VacationPlaceDTO>().toDomainModel()
            Log.d(TAG, "Successfully added place to vacation")
            Result.success(vacationPlace)

        } catch (e: Exception) {
            Log.e(TAG, "Error adding place to vacation: ${e.message}", e)
            Log.e(TAG, "Stack trace:", e)

            if (e.message?.contains("unique constraint", ignoreCase = true) == true ||
                e.message?.contains("duplicate", ignoreCase = true) == true) {
                Result.failure(Exception(resourceProvider.getString(R.string.error_place_already_in_vacation)))
            } else {
                Result.failure(Exception(resourceProvider.getString(R.string.error_adding_place_to_vacation)))
            }
        }
    }

    override suspend fun searchVacations(query: String): Result<List<Vacation>> {
        return try {
            Log.d(TAG, "Searching ALL public vacations with query: $query")

            val vacationsResponse = supabaseClient.client.postgrest
                .from("vacations")
                .select {
                    filter {
                        ilike("title", "%${query.trim()}%")
                    }
                    order("created_at", order = Order.DESCENDING)
                }

            val vacationDTOs = vacationsResponse.decodeList<VacationDTO>()
            Log.d(TAG, "Found ${vacationDTOs.size} vacations from all users")

            val vacationsWithCount = vacationDTOs.map { vacationDTO ->
                val placesCount = try {
                    val placesResponse = supabaseClient.client.postgrest
                        .from("vacation_places")
                        .select(Columns.raw("id")) {
                            filter {
                                eq("vacation_id", vacationDTO.id)
                            }
                        }
                    placesResponse.decodeList<VacationIdDTO>().size
                } catch (_: Exception) {
                    0
                }

                Vacation(
                    id = vacationDTO.id,
                    userId = vacationDTO.userId,
                    title = vacationDTO.title,
                    createdAt = vacationDTO.createdAt,
                    placesCount = placesCount
                )
            }

            Log.d(TAG, "Returning ${vacationsWithCount.size} vacations")
            Result.success(vacationsWithCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching vacations: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_loading_vacations)))
        }
    }

    override suspend fun getVacationWithUser(vacationId: String): Result<Pair<Vacation, String>> {
        return try {
            val currentUser = supabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val vacationResponse = supabaseClient.client.postgrest
                .from("vacations")
                .select {
                    filter {
                        eq("id", vacationId)
                    }
                    limit(1)
                }

            val vacationDTO = vacationResponse.decodeSingle<VacationDTO>()

            val username = if (vacationDTO.userId == currentUser.id) {
                resourceProvider.getString(R.string.you)
            } else {
                try {
                    val userResponse = supabaseClient.client.postgrest
                        .from("users")
                        .select(Columns.raw("username")) {
                            filter {
                                eq("auth_id", vacationDTO.userId)
                            }
                            limit(1)
                        }

                    val user = userResponse.decodeSingle<UserInfo>()
                    user.username
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting user info: ${e.message}", e)
                    "Unknown User"
                }
            }

            val placesCount = try {
                val placesResponse = supabaseClient.client.postgrest
                    .from("vacation_places")
                    .select(Columns.raw("id")) {
                        filter {
                            eq("vacation_id", vacationId)
                        }
                    }
                placesResponse.decodeList<VacationIdDTO>().size
            } catch (_: Exception) {
                0
            }

            val vacation = Vacation(
                id = vacationDTO.id,
                userId = vacationDTO.userId,
                title = vacationDTO.title,
                createdAt = vacationDTO.createdAt,
                placesCount = placesCount
            )

            Result.success(Pair(vacation, username))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vacation with user: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_loading_vacation)))
        }
    }

    override suspend fun getVacationPlaceIds(vacationId: String): Result<List<String>> {
        return try {
            val placesResponse = supabaseClient.client.postgrest
                .from("vacation_places")
                .select(Columns.raw("place_id, order_index")) {
                    filter {
                        eq("vacation_id", vacationId)
                    }
                    order("order_index", order = Order.ASCENDING)
                }

            val placeIds = placesResponse.decodeList<VacationPlaceSimpleDTO>()
                .map { it.placeId }

            Result.success(placeIds)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vacation place IDs: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_loading_places)))
        }
    }

    override suspend fun removePlaceFromVacation(vacationId: String, placeId: String): Result<Unit> {
        return try {
            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val vacationCheck = supabaseClient.client.postgrest
                .from("vacations")
                .select(Columns.raw("id")) {
                    filter {
                        eq("id", vacationId)
                        eq("user_id", currentUserId)
                    }
                    limit(1)
                }

            val vacationExists = try {
                vacationCheck.decodeList<VacationIdDTO>().isNotEmpty()
            } catch (_: Exception) {
                false
            }

            if (!vacationExists) {
                return Result.failure(Exception(resourceProvider.getString(R.string.error_vacation_not_found)))
            }

            supabaseClient.client.postgrest
                .from("vacation_places")
                .delete {
                    filter {
                        eq("vacation_id", vacationId)
                        eq("place_id", placeId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing place from vacation: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_removing_place)))
        }
    }

    override suspend fun getVacationComments(vacationId: String): Result<List<VacationComment>> {
        return try {

            val response = supabaseClient.client.postgrest
                .from("vacation_comments")
                .select(Columns.raw("id, vacation_id, user_id, text, created_at")) {
                    filter {
                        eq("vacation_id", vacationId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }

            val commentDTOs = response.decodeList<VacationCommentDTO>()

            val comments = commentDTOs.map { dto ->
                val username = try {
                    val userResponse = supabaseClient.client.postgrest
                        .from("users")
                        .select(Columns.raw("username")) {
                            filter {
                                eq("auth_id", dto.userId)
                            }
                            limit(1)
                        }

                    val userInfo = userResponse.decodeSingle<UserInfo>()
                    userInfo.username
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching username for user ${dto.userId}: ${e.message}", e)
                    resourceProvider.getString(R.string.unknown_user)
                }

                VacationComment(
                    id = dto.id,
                    vacationId = dto.vacationId,
                    userId = dto.userId,
                    userName = username,
                    text = dto.text,
                    createdAt = dto.createdAt,
                    isOwner = dto.userId == supabaseClient.client.auth.currentUserOrNull()?.id
                )
            }

            Result.success(comments)
        } catch (e: SerializationException) {
            Log.e(TAG, "Serialization error: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_parsing_comments)))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vacation comments: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.unknown_error)))
        }
    }

    override suspend fun addVacationComment(
        vacationId: String,
        text: String
    ): Result<VacationComment> {
        return try {
            val currentUser = supabaseClient.client.auth.currentUserOrNull()
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val commentToInsert = VacationCommentInsertDTO(
                vacationId = vacationId,
                userId = currentUser.id,
                text = text.trim()
            )

            val response = supabaseClient.client.postgrest
                .from("vacation_comments")
                .insert(commentToInsert) {
                    select(Columns.raw("id, vacation_id, user_id, text, created_at"))
                }

            val dto = response.decodeSingle<VacationCommentDTO>()

            val username = try {
                val userResponse = supabaseClient.client.postgrest
                    .from("users")
                    .select(Columns.raw("username")) {
                        filter {
                            eq("auth_id", currentUser.id)
                        }
                        limit(1)
                    }

                val userInfo = userResponse.decodeSingle<UserInfo>()
                userInfo.username
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching username: ${e.message}", e)
                "Unknown User"
            }

            val comment = VacationComment(
                id = dto.id,
                vacationId = dto.vacationId,
                userId = dto.userId,
                userName = username,
                text = dto.text,
                createdAt = dto.createdAt,
                isOwner = true
            )

            Result.success(comment)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding vacation comment: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_posting_comment)))
        }
    }

    override suspend fun updateVacationComment(
        commentId: String,
        text: String
    ): Result<Unit> {
        return try {
            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            supabaseClient.client.postgrest
                .from("vacation_comments")
                .update({
                    set("text", text.trim())
                }) {
                    filter {
                        eq("id", commentId)
                        eq("user_id", currentUserId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vacation comment: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_updating_comment)))
        }
    }

    override suspend fun deleteVacationComment(commentId: String): Result<Unit> {
        return try {
            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            supabaseClient.client.postgrest
                .from("vacation_comments")
                .delete {
                    filter {
                        eq("id", commentId)
                        eq("user_id", currentUserId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting vacation comment: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.error_deleting_comment)))
        }
    }

    override suspend fun getVacationCommentsCount(vacationId: String): Result<Int> {
        return try {
            val response = supabaseClient.client.postgrest
                .from("vacation_comments")
                .select(Columns.raw("id")) {
                    filter {
                        eq("vacation_id", vacationId)
                    }
                }

            val count = response.decodeList<VacationIdDTO>().size
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vacation comments count: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun saveVacation(vacationId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            supabaseClient.client.from("saved_vacations")
                .insert(
                    mapOf(
                        "user_id" to userId,
                        "vacation_id" to vacationId
                    )
                )

            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(Exception(resourceProvider.getString(R.string.failed_to_save_vacation)))
        }
    }

    override suspend fun unsaveVacation(vacationId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            supabaseClient.client.from("saved_vacations")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("vacation_id", vacationId)
                    }
                }

            Result.success(Unit)
        } catch (_: Exception) {
            Result.failure(Exception(resourceProvider.getString(R.string.failed_to_unsave_vacation)))
        }
    }

    override suspend fun getSavedVacations(): Result<List<Vacation>> {
        return try {
            val userId = supabaseClient.client.auth.currentUserOrNull()?.id
            if (userId == null) {
                return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))
            }

            val savedVacationDtos = supabaseClient.client.from("saved_vacations")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SavedVacationDTO>()

            Log.d(TAG, "getSavedVacations: Found ${savedVacationDtos.size} saved vacation records")

            if (savedVacationDtos.isEmpty()) {
                Log.d(TAG, "getSavedVacations: No saved vacations, returning empty list")
                return Result.success(emptyList())
            }

            val vacations = savedVacationDtos.mapNotNull { savedDto ->
                try {

                    val vacationDto = supabaseClient.client.from("vacations")
                        .select {
                            filter {
                                eq("id", savedDto.vacationId)
                            }
                        }
                        .decodeSingle<VacationDTO>()

                    val placesCount = try {
                        supabaseClient.client.from("vacation_places")
                            .select(Columns.raw("id")) {
                                filter {
                                    eq("vacation_id", vacationDto.id)
                                }
                            }
                            .decodeList<VacationIdDTO>()
                            .size
                    } catch (e: Exception) {
                        Log.e(TAG, "getSavedVacations: Error getting places count: ${e.message}")
                        0
                    }

                    val commentsCount = try {
                        supabaseClient.client.from("vacation_comments")
                            .select(Columns.raw("id")) {
                                filter {
                                    eq("vacation_id", vacationDto.id)
                                }
                            }
                            .decodeList<VacationIdDTO>()
                            .size
                    } catch (e: Exception) {
                        Log.e(TAG, "getSavedVacations: Error getting comments count: ${e.message}")
                        0
                    }

                    val vacation = vacationDto.toDomainModelWithSaved(
                        isSaved = true,
                        placesCount = placesCount,
                        commentsCount = commentsCount
                    )
                    vacation
                } catch (e: Exception) {
                    Log.e(TAG, "getSavedVacations: Error fetching vacation ${savedDto.vacationId}: ${e.message}", e)
                    null
                }
            }
            vacations.forEach { v ->
                Log.d(TAG, "getSavedVacations: Final vacation - ${v.title}, isSaved=${v.isSaved}")
            }

            Result.success(vacations)
        } catch (e: Exception) {
            Log.e(TAG, "getSavedVacations: Critical error: ${e.message}", e)
            Result.failure(Exception(resourceProvider.getString(R.string.failed_to_load_saved_vacations)))
        }
    }

    override suspend fun isVacationSaved(vacationId: String): Result<Boolean> {
        return try {
            val userId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val result = supabaseClient.client.from("saved_vacations")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("vacation_id", vacationId)
                    }
                }
                .decodeList<SavedVacationDTO>()

            Result.success(result.isNotEmpty())
        } catch (_: Exception) {
            Result.failure(Exception(resourceProvider.getString(R.string.failed_to_check_saved_status)))
        }
    }
}