package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.data.DataItem
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.data.ExtractionException
import com.tealium.prism.firebase.helpers.assertFailure
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetSessionTimeoutCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
    private val command = setSessionTimeoutCommand(firebase)

    @Test
    fun forwards_seconds_as_milliseconds() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", 1800.0) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setSessionTimeoutDuration(1_800_000L) }
    }

    @Test
    fun accepts_string_values_via_lenient_converter() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", "3600") })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setSessionTimeoutDuration(3_600_000L) }
    }

    @Test
    fun accepts_int_values_via_lenient_converter() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", 1800) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setSessionTimeoutDuration(1_800_000L) }
    }

    @Test
    fun missing_parameter_fails_with_missing() {
        val result = runCommand(command, DataObject.create {})

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("session_timeout_seconds", error.key)
        verify(exactly = 0) { firebase.setSessionTimeoutDuration(any()) }
    }

    @Test
    fun null_value_fails_with_missing() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", DataItem.NULL) })

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("session_timeout_seconds", error.key)
        verify(exactly = 0) { firebase.setSessionTimeoutDuration(any()) }
    }

    @Test
    fun non_numeric_value_fails_with_type_mismatch() {
        val result = runCommand(command, DataObject.create { put("session_timeout_seconds", "not_a_number") })

        val error = result.assertFailure<ExtractionException.TypeMismatch>()
        assertEquals("session_timeout_seconds", error.key)
        verify(exactly = 0) { firebase.setSessionTimeoutDuration(any()) }
    }
}
