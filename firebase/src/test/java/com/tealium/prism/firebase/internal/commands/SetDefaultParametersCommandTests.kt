package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetDefaultParametersCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = setDefaultParametersCommand(firebase)

    @Test
    fun builds_bundle_from_parameters() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "parameters",
                    DataObject.create {
                        put("app_version", "2.1.0")
                        put("build", 42L)
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        assertNotNull(firebase.lastDefaultParameters)
        assertEquals("2.1.0", firebase.lastDefaultParameters!!.getString("app_version"))
        assertEquals(42L, firebase.lastDefaultParameters!!.getLong("build"))
    }

    @Test
    fun missing_parameters_clears_defaults() {
        val result = runCommand(command, DataObject.create {})
        assertTrue(result.isSuccess)
        assertNull(firebase.lastDefaultParameters)
        assertEquals(1, firebase.setDefaultEventParametersCount)
    }

    @Test
    fun empty_parameters_object_is_noop() {
        val result = runCommand(
            command,
            DataObject.create { put("parameters", DataObject.create {}) },
        )
        assertTrue(result.isSuccess)
        assertEquals(0, firebase.setDefaultEventParametersCount)
    }

    @Test
    fun mixed_scalar_types_preserved() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "parameters",
                    DataObject.create {
                        put("string_key", "value")
                        put("long_key", 42L)
                        put("double_key", 9.99)
                        put("bool_key", true)
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val bundle = firebase.lastDefaultParameters!!
        assertEquals("value", bundle.getString("string_key"))
        assertEquals(42L, bundle.getLong("long_key"))
        assertEquals(9.99, bundle.getDouble("double_key"), 0.0)
        assertEquals(true, bundle.getBoolean("bool_key"))
    }
}
