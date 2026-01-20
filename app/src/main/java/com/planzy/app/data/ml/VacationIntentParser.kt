package com.planzy.app.data.ml

import android.content.Context
import com.planzy.app.data.model.VacationIntent
import com.planzy.app.data.model.VacationPreferences
import com.planzy.app.data.util.ResourceProvider
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.planzy.app.R
import com.google.mlkit.nl.entityextraction.Entity

class VacationIntentParser(
    private val context: Context,
    private val recourseProvider: ResourceProvider
) {
    companion object {
        private val DESTINATION_REGEX = Regex("""(?i)\b(?:in|to|at)\b\s+([A-Z][a-z]+)""")
        private val NUMBERS_REGEX = Regex("""\d+""")
        private val WORDS_SPLIT_REGEX = Regex("""\W+""")
    }
    private val entityExtractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
    )
    private val json = Json { ignoreUnknownKeys = true }
    private var learnedData: LearnedThemeData = loadLearnedData()

    @Serializable
    data class LearnedThemeData(
        val themeWordFrequency: Map<String, Map<String, Int>> = emptyMap(),
        val themePreferencePatterns: Map<String, PreferencePattern> = emptyMap()
    )

    @Serializable
    data class PreferencePattern(
        val avgRestaurantsPerDay: Double = 2.0,
        val avgAttractionsPerDay: Double = 2.0,
        val avgNightlifePerDay: Double = 0.0,
        val categoryFilter: String? = null
    )

    init {
        entityExtractor.downloadModelIfNeeded()
        if (learnedData.themeWordFrequency.isEmpty()) bootstrapThemeData()
    }

    private fun bootstrapThemeData() {
        learnedData = LearnedThemeData(
            themeWordFrequency = mapOf(
                "historical" to mapOf(
                    "history" to 10,
                    "museum" to 8,
                    "historical" to 10),
                "beach" to mapOf(
                    "beach" to 10,
                    "sea" to 10,
                    "ocean" to 5),
                "nightlife" to mapOf(
                    "nightlife" to 10,
                    "party" to 10,
                    "club" to 10,
                    "bar" to 10
                )
            ),
            themePreferencePatterns = mapOf(
                "historical" to PreferencePattern(
                    2.0,
                    2.0,
                    0.0,
                    "historical_sites"),
                "beach" to PreferencePattern(
                    2.0,
                    1.0,
                    0.5,
                    "beaches"),
                "nightlife" to PreferencePattern(
                    2.0,
                    1.0,
                    2.0,
                    "nightlife")
            )
        )
        saveLearnedData()
    }

    suspend fun parseIntent(userMessage: String): Result<VacationIntent> {
        return try {
            val entities = extractEntities(userMessage)
            val destination = extractDestination(entities, userMessage) ?: recourseProvider.getString(R.string.unknown)
            val duration = extractNumbers(userMessage).firstOrNull { it in 1..30 } ?: 3
            val theme = classifyTheme(userMessage)

            val isNightlife = theme == "nightlife" || userMessage.contains("night", true) || userMessage.contains("bar", true)

            Result.success(VacationIntent(destination, duration, theme, buildPreferences(theme, duration, isNightlife)))
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun extractEntities(text: String) =
        entityExtractor.annotate(EntityExtractionParams.Builder(text).build()).await()

    private fun extractDestination(entities: List<EntityAnnotation>, text: String): String? {
        entities.forEach { ann ->
            ann.entities.forEach {
                if (it.type == Entity.TYPE_ADDRESS || it.type == 4) return ann.annotatedText
            }
        }
        return DESTINATION_REGEX.find(text)?.groupValues?.get(1)
    }

    private fun extractNumbers(text: String) = NUMBERS_REGEX.findAll(text).mapNotNull { it.value.toIntOrNull() }.toList()

    private fun classifyTheme(text: String): String? {
        val words = text.lowercase().split(WORDS_SPLIT_REGEX)
        return learnedData.themeWordFrequency.maxByOrNull { entry ->
            words.sumOf { word -> entry.value[word] ?: 0 }
        }?.takeIf { entry -> words.any { entry.value.containsKey(it) } }?.key
    }

    private fun buildPreferences(theme: String?, d: Int, explicitNight: Boolean): VacationPreferences {
        val p = learnedData.themePreferencePatterns[theme] ?: PreferencePattern()
        return VacationPreferences(
            hotelCount = 1,
            restaurantCount = (p.avgRestaurantsPerDay * d).toInt().coerceAtLeast(1),
            attractionCount = (p.avgAttractionsPerDay * d).toInt().coerceAtLeast(1),
            nightlifeCount = if (explicitNight) (if(p.avgNightlifePerDay > 0) p.avgNightlifePerDay * d else d).toInt().coerceAtLeast(1) else 0
        )
    }

    private fun saveLearnedData() {
        val prefs = context.getSharedPreferences("ml_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString(
                "data",
                json.encodeToString(LearnedThemeData.serializer(), learnedData)
            )
        }
    }

    private fun loadLearnedData(): LearnedThemeData {
        val prefs = context.getSharedPreferences("ml_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("data", null) ?: return LearnedThemeData()
        return try { json.decodeFromString(LearnedThemeData.serializer(), raw) } catch (e: Exception) { LearnedThemeData() }
    }
}