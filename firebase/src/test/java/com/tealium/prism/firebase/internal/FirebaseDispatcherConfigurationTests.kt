package com.tealium.prism.firebase.internal

import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.FirebaseLogLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseDispatcherConfigurationTests {

    @Test
    fun fromDataObject_reads_all_keys() {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(
            DataObject.create {
                put("session_timeout_seconds", 1800)
                put("analytics_collection_enabled", true)
                put("log_level", "debug")
            }
        )
        assertEquals(1800.0, cfg.sessionTimeoutSeconds)
        assertEquals(true, cfg.analyticsCollectionEnabled)
        assertEquals(FirebaseLogLevel.DEBUG, cfg.logLevel)
    }

    @Test
    fun fromDataObject_accepts_string_timeout_via_lenient_converter() {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(
            DataObject.create { put("session_timeout_seconds", "3600") }
        )
        assertEquals(3600.0, cfg.sessionTimeoutSeconds)
    }

    @Test
    fun fromDataObject_returns_nulls_for_missing_keys() {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(DataObject.create {})
        assertNull(cfg.sessionTimeoutSeconds)
        assertNull(cfg.analyticsCollectionEnabled)
        assertNull(cfg.logLevel)
    }

    @Test
    fun fromDataObject_reads_log_level() {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(
            DataObject.create { put("log_level", "debug") }
        )
        assertEquals(FirebaseLogLevel.DEBUG, cfg.logLevel)
    }

    @Test
    fun fromDataObject_log_level_is_case_insensitive() {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(
            DataObject.create { put("log_level", "WARNING") }
        )
        assertEquals(FirebaseLogLevel.WARNING, cfg.logLevel)
    }

    @Test
    fun fromDataObject_unknown_log_level_returns_null() {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(
            DataObject.create { put("log_level", "bogus") }
        )
        assertNull(cfg.logLevel)
    }
}
