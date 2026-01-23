package com.planzy.app.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.SupabaseClient

object SupabaseClient {

    private const val SUPABASE_URL = "https://pjngaenofksdgnuitqut.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_4HKq4fc5eGbwQaVl37geDg_9U-0_dfg"

    lateinit var client: SupabaseClient
        private set

    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        client = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            httpEngine = Android.create {
                connectTimeout = 30_000
                socketTimeout = 30_000
            }

            install(Auth.Companion) {
                scheme = "planzy"
                host = "auth-callback"
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
                autoSaveToStorage = true
            }

            install(Postgrest.Companion)
            install(Storage)
        }

        isInitialized = true
    }
}