package com.planzy.app.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android

object SupabaseClient {

    private const val SUPABASE_URL = "https://pjngaenofksdgnuitqut.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_4HKq4fc5eGbwQaVl37geDg_9U-0_dfg"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        httpEngine = Android.create {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }

        install(Auth) {
            scheme = "planzy"
            host = "auth-callback"
            alwaysAutoRefresh = true
        }

        install(Postgrest)
    }
}