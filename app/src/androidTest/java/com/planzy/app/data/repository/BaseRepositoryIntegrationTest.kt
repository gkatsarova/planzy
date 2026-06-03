package com.planzy.app.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.planzy.app.data.remote.SupabaseClient
import com.planzy.app.data.util.ResourceProviderImpl
import org.junit.Before

abstract class BaseRepositoryIntegrationTest {

    @Before
    fun initBaseDependencies() {
        SupabaseClient.initialize()
    }

    protected fun getContext(): Context {
        return ApplicationProvider.getApplicationContext()
    }

    protected fun getResourceProvider(): ResourceProviderImpl {
        return ResourceProviderImpl(getContext())
    }
}
