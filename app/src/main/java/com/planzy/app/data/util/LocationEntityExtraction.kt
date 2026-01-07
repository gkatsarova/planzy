package com.planzy.app.data.util

import com.google.mlkit.nl.entityextraction.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationEntityExtractor {
    private var extractor: EntityExtractor? = null

    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
        extractor = EntityExtraction.getClient(options)
        extractor?.downloadModelIfNeeded()
            ?.addOnSuccessListener { continuation.resume(true) }
            ?.addOnFailureListener { continuation.resume(false) }
    }

    suspend fun extractLocation(query: String): LocationInfo? = suspendCancellableCoroutine { continuation ->
        if (extractor == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val params = EntityExtractionParams.Builder(query).build()
        extractor?.annotate(params)
            ?.addOnSuccessListener { result ->
                var foundInfo: LocationInfo? = null

                result.forEach { entityAnnotation ->
                    entityAnnotation.entities.forEach { entity ->
                        if (entity.type == Entity.TYPE_ADDRESS) {
                            foundInfo = LocationInfo(
                                hasLocation = true,
                                locationText = entityAnnotation.annotatedText,
                                type = LocationType.ADDRESS
                            )
                            return@forEach
                        }
                    }
                    if (foundInfo != null) return@forEach
                }
                continuation.resume(foundInfo)
            }
            ?.addOnFailureListener {
                continuation.resume(null)
            }
    }
}

enum class LocationType {ADDRESS}
data class LocationInfo(val hasLocation: Boolean, val locationText: String, val type: LocationType)