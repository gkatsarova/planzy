package com.planzy.app.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.planzy.app.BuildConfig

object SupabaseClient {

    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    lateinit var client: SupabaseClient
        private set

    val sessionFlow: Flow<SessionStatus>
        get() = client.auth.sessionStatus

    val currentUserIdFlow: Flow<String?>
        get() = sessionFlow.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.id
                else -> null
            }
        }

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