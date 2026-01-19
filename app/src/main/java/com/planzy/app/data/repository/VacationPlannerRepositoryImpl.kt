package com.planzy.app.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.planzy.app.R
import com.planzy.app.data.ml.VacationIntentParser
import com.planzy.app.data.model.*
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.remote.TripadvisorApi
import com.planzy.app.data.util.ResourceProvider
import com.planzy.app.domain.model.Place
import com.planzy.app.domain.model.Vacation
import com.planzy.app.domain.repository.VacationPlannerRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class VacationPlannerRepositoryImpl(
    private val intentParser: VacationIntentParser,
    private val tripadvisorApi: TripadvisorApi,
    private val supabaseClient: SupabaseClient,
    private val resourceProvider: ResourceProvider
) : VacationPlannerRepository {

    private val TAG = VacationPlannerRepositoryImpl::class.java.simpleName

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createVacationFromText(userMessage: String): Result<VacationPlannerResponse> {
        return try {
            Log.d(TAG, "Starting creation for: $userMessage")

            val intent = intentParser.parseIntent(userMessage).getOrElse {
                return Result.success(VacationPlannerResponse.Error(resourceProvider.getString(R.string.error_analyzing)))
            }

            val userId = supabaseClient.client.auth.currentUserOrNull()?.id ?: return Result.success(VacationPlannerResponse.Error(resourceProvider.getString(R.string.error_user_not_logged_in)))

            val search = tripadvisorApi.searchLocations(intent.destination).getOrNull()
            val firstResult = search?.data?.firstOrNull() ?: return Result.success(VacationPlannerResponse.Error(resourceProvider.getString(R.string.error_city_not_found)))

            val destDetails = tripadvisorApi.getLocationDetails(firstResult.locationId).getOrNull() ?: return Result.success(VacationPlannerResponse.Error(resourceProvider.getString(R.string.where_is_your_dream_vacation)))
            val latLong = "${destDetails.latitude},${destDetails.longitude}"

            val allPlaces = mutableListOf<Place>()

            val categoriesToSearch = mutableListOf<Triple<PlaceCategory, Int, String?>>()

            categoriesToSearch.add(Triple(PlaceCategory.HOTEL, intent.preferences.hotelCount, null))

            if (intent.preferences.nightlifeCount > 0) {
                val halfLimit = (intent.preferences.nightlifeCount / 2).coerceAtLeast(1)

                categoriesToSearch.add(Triple(PlaceCategory.ATTRACTION, halfLimit, "bar, pub, cocktail lounge"))
                categoriesToSearch.add(Triple(PlaceCategory.ATTRACTION, halfLimit, "night club, dance club, disco"))
            }

            categoriesToSearch.add(Triple(PlaceCategory.RESTAURANT, intent.preferences.restaurantCount, null))
            categoriesToSearch.add(Triple(PlaceCategory.ATTRACTION, intent.preferences.attractionCount, null))

            for ((cat, count, query) in categoriesToSearch) {
                Log.d(TAG, "Requesting: ${cat.apiValue} | Query: $query | Count: $count")

                val nearby = tripadvisorApi.searchNearbyForPlanner(
                    latLong = latLong,
                    category = cat.apiValue,
                    subCategory = query,
                    limit = count
                ).getOrNull()

                nearby?.data?.take(count)?.forEach { loc ->
                    val details = tripadvisorApi.getLocationDetails(loc.locationId).getOrNull()
                    details?.let {
                        var place = it.toDomainModel()
                        if (place.photoUrl.isNullOrEmpty()) {
                            val photo = tripadvisorApi.getLocationPhotos(loc.locationId).getOrNull()?.data?.firstOrNull()
                            photo?.let { p -> place = place.copy(photoUrl = p.images?.large?.url) }
                        }
                        allPlaces.add(place)
                    }
                }
            }

            val uniquePlaces = allPlaces.distinctBy { it.id }

            uniquePlaces.forEach { place ->
                supabaseClient.client.postgrest["places"].upsert(place.toDTO()) { onConflict = "location_id" }
            }

            val vacationId = UUID.randomUUID().toString()
            val vacationDTO = VacationDTO(vacationId, userId, "Trip to ${intent.destination}", java.time.Instant.now().toString())
            supabaseClient.client.postgrest["vacations"].insert(vacationDTO)

            val links = uniquePlaces.mapIndexed { i, p -> VacationPlaceInsertDTO(vacationId, p.id, i) }
            supabaseClient.client.postgrest["vacation_places"].insert(links)

            return Result.success(VacationPlannerResponse.Success(
                vacation = Vacation(vacationId, userId, vacationDTO.title, vacationDTO.createdAt, uniquePlaces.size),
                placesAdded = uniquePlaces.size,
                message = resourceProvider.getString(R.string.success_vacation_created)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Result.success(VacationPlannerResponse.Error(resourceProvider.getString(R.string.error_creating_vacation)))
        }
    }
}