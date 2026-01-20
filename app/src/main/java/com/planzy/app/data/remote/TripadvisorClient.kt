package com.planzy.app.data.remote

import com.planzy.app.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object TripadvisorClient {

    private const val BASE_URL = "https://api.content.tripadvisor.com/api/v1"
    private const val TIMEOUT_MILLIS = 30_000L

    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                prettyPrint = true
            })
        }

        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }

        defaultRequest {
            headers.append("accept", "application/json")
            headers.append("User-Agent", "Planzy-Android/${BuildConfig.VERSION_NAME}")
        }
    }

    fun getApiKey(): String = BuildConfig.TRIPADVISOR_API_KEY

    fun getBaseUrl(): String = BASE_URL
}