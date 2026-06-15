package com.tealium.prism.firebase

import com.tealium.prism.core.api.misc.TimeFrame
import com.tealium.prism.firebase.internal.FirebaseDispatcherConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class FirebaseSettingsBuilderTests {

    @Test
    fun module_type_is_firebase() {
        val built = FirebaseSettingsBuilder().build()
        assertEquals(Firebase.ID, built.getString("module_type"))
    }

    @Test
    fun setSessionTimeout_stores_seconds_in_configuration() {
        val built = FirebaseSettingsBuilder()
            .setSessionTimeout(TimeFrame(30, TimeUnit.MINUTES))
            .build()
        val cfg = built.getDataObject("configuration")!!
        assertEquals(1_800L, cfg.getLong(FirebaseDispatcherConfiguration.KEY_SESSION_TIMEOUT))
    }

    @Test
    fun setAnalyticsEnabled_stores_boolean_in_configuration() {
        val built = FirebaseSettingsBuilder()
            .setAnalyticsEnabled(false)
            .build()
        val cfg = built.getDataObject("configuration")!!
        assertEquals(false, cfg.getBoolean(FirebaseDispatcherConfiguration.KEY_ANALYTICS_ENABLED))
    }

    @Test
    fun all_settings_stored_together_in_configuration() {
        val built = FirebaseSettingsBuilder()
            .setSessionTimeout(TimeFrame(30, TimeUnit.MINUTES))
            .setAnalyticsEnabled(true)
            .build()
        val cfg = built.getDataObject("configuration")!!
        assertEquals(1_800L, cfg.getLong(FirebaseDispatcherConfiguration.KEY_SESSION_TIMEOUT))
        assertEquals(true, cfg.getBoolean(FirebaseDispatcherConfiguration.KEY_ANALYTICS_ENABLED))
    }

    @Test
    fun empty_builder_produces_empty_configuration() {
        val built = FirebaseSettingsBuilder().build()
        val cfg = built.getDataObject("configuration")!!
        assertEquals(null, cfg.getString(FirebaseDispatcherConfiguration.KEY_SESSION_TIMEOUT))
        assertEquals(null, cfg.getBoolean(FirebaseDispatcherConfiguration.KEY_ANALYTICS_ENABLED))
    }
}
