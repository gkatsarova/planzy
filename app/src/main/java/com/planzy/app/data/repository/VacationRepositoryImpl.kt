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
                } catch (e: Exception) {
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
            } catch (e: Exception) {
                false
            }

            if (!vacationExists) {
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
            } catch (e: Exception) {
                false
            }

            if (placeAlreadyExists) {
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
            } catch (e: Exception) {
                0
            }

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
            Result.success(vacationPlace)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding place to vacation: ${e.message}", e)

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
            val currentUserId = supabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception(resourceProvider.getString(R.string.error_user_not_logged_in)))

            Log.d(TAG, "Searching vacations with query: $query for user: $currentUserId")

            val vacationsResponse = supabaseClient.client.postgrest
                .from("vacations")
                .select {
                    filter {
                        eq("user_id", currentUserId)
                        ilike("title", "%${query.trim()}%")
                    }
                    order("created_at", order = Order.DESCENDING)
                }

            val vacationDTOs = vacationsResponse.decodeList<VacationDTO>()
            Log.d(TAG, "Found ${vacationDTOs.size} vacations")

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
                } catch (e: Exception) {
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

            Log.d(TAG, "Returning vacations: ${vacationsWithCount.map { it.title }}")
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
                currentUser.userMetadata?.get("username") as? String ?: "You"
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
            } catch (e: Exception) {
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
            } catch (e: Exception) {
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
}