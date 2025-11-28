package com.planzy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.planzy.app.ui.navigation.Navigation
import com.planzy.app.ui.theme.PlanzyTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://pjngaenofksdgnuitqut.supabase.co",
    supabaseKey = "sb_publishable_4HKq4fc5eGbwQaVl37geDg_9U-0_dfg"
) {
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlanzyTheme {
                    Navigation()
            }
        }
    }
}
