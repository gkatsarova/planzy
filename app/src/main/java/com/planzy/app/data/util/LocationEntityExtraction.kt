package com.planzy.app.data.util

import android.util.Log
import com.google.mlkit.nl.entityextraction.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationEntityExtractor {
    private var extractor: EntityExtractor? = null

    suspend fun initialize() = suspendCancellableCoroutine { continuation ->
        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
        extractor = EntityExtraction.getClient(options)

        extractor?.downloadModelIfNeeded()
            ?.addOnSuccessListener { continuation.resume(Unit) }
            ?.addOnFailureListener {
                Log.e("MLKit", "Model download failed: ${it.message}")
                continuation.resume(Unit)
            }
    }

    suspend fun extractLocation(text: String): EntityAnnotation? = suspendCancellableCoroutine { continuation ->
        val params = EntityExtractionParams.Builder(text).build()

        extractor?.annotate(params)
            ?.addOnSuccessListener { annotations ->
                continuation.resume(annotations.firstOrNull())
            }
            ?.addOnFailureListener { e ->
                Log.e("MLKit", "Extraction failed: ${e.message}")
                continuation.resume(null)
            }
    }
}