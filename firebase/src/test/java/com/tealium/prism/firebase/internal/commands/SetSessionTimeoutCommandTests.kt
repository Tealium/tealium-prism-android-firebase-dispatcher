package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetSessionTimeoutCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = setSessionTimeoutCommand(firebase)

    @Test
    fun forwards_seconds_as_milliseconds() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", 1800.0) })
        assertTrue(result.isSuccess)
        assertEquals(1_800_000L, firebase.lastSessionTimeoutMillis)
    }

    @Test
    fun accepts_string_values_via_lenient_converter() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", "3600") })
        assertTrue(result.isSuccess)
        assertEquals(3_600_000L, firebase.lastSessionTimeoutMillis)
    }

    @Test
    fun accepts_int_values_via_lenient_converter() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", 1800) })
        assertTrue(result.isSuccess)
        assertEquals(1_800_000L, firebase.lastSessionTimeoutMillis)
    }

    @Test
    fun missing_parameter_fails_with_missing_parameter() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("missing"))
        assertEquals(0, firebase.setSessionTimeoutCount)
    }

    @Test
    fun non_numeric_value_fails_with_invalid_parameter_type() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", "not_a_number") })
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("expected type"))
        assertEquals(0, firebase.setSessionTimeoutCount)
    }
}
