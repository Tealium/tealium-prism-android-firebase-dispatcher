package com.tealium.prism.firebase.internal.commands

import android.os.Bundle
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetDefaultParametersCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
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
        val bundle = slot<Bundle>()
        verify(exactly = 1) { firebase.setDefaultEventParameters(capture(bundle)) }
        assertEquals("2.1.0", bundle.captured.getString("app_version"))
        assertEquals(42L, bundle.captured.getLong("build"))
    }

    @Test
    fun missing_parameters_clears_defaults() {
        val result = runCommand(command, DataObject.create {})
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setDefaultEventParameters(null) }
    }

    @Test
    fun empty_parameters_object_is_noop() {
        val result = runCommand(
            command,
            DataObject.create { put("parameters", DataObject.create {}) },
        )
        assertTrue(result.isSuccess)
        verify(exactly = 0) { firebase.setDefaultEventParameters(any()) }
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
        val bundle = slot<Bundle>()
        verify(exactly = 1) { firebase.setDefaultEventParameters(capture(bundle)) }
        assertEquals("value", bundle.captured.getString("string_key"))
        assertEquals(42L, bundle.captured.getLong("long_key"))
        assertEquals(9.99, bundle.captured.getDouble("double_key"), 0.0)
        assertEquals(true, bundle.captured.getBoolean("bool_key"))
    }
}
