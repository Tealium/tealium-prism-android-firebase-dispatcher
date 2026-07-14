package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetUserPropertyCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
    private val command = setUserPropertyCommand(firebase)

    @Test
    fun single_property_forwarded() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", "tier")
                put("property_value", "premium")
            },
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserProperty("tier", "premium") }
    }

    @Test
    fun empty_value_clears_property() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", "tier")
                put("property_value", "")
            },
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserProperty("tier", null) }
    }

    @Test
    fun arrays_set_multiple_properties() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.fromStringCollection(listOf("tier", "level")))
                put("property_value", DataList.fromStringCollection(listOf("premium", "expert")))
            },
        )
        assertTrue(result.isSuccess)
        verifySequence {
            firebase.setUserProperty("tier", "premium")
            firebase.setUserProperty("level", "expert")
        }
    }

    @Test
    fun array_length_mismatch_fails() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.fromStringCollection(listOf("tier", "level")))
                put("property_value", DataList.fromStringCollection(listOf("premium")))
            },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun missing_name_fails() {
        val result = runCommand(
            command,
            DataObject.create { put("property_value", "premium") },
        )
        assertFalse(result.isSuccess)
    }

    @Test
    fun coerces_numeric_name_and_value_to_strings() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", 42)
                put("property_value", 99)
            },
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserProperty("42", "99") }
    }

    @Test
    fun coerces_double_value_to_string() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", "score")
                put("property_value", 9.5)
            },
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserProperty("score", "9.5") }
    }

    @Test
    fun all_null_names_array_fails_with_emptyArray() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.fromStringCollection(listOf<String?>(null, null)))
                put("property_value", DataList.fromStringCollection(listOf("value_a", "value_b")))
            },
        )
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("empty"))
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun missing_value_fails() {
        val result = runCommand(
            command,
            DataObject.create { put("property_name", "tier") },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun unconvertible_name_fails() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataObject.create { put("nested", "object") })
                put("property_value", "premium")
            },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun arrays_with_empty_value_clear_property() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "property_name",
                    DataList.fromStringCollection(listOf("tier", "level")),
                )
                put(
                    "property_value",
                    DataList.fromStringCollection(listOf("", "expert")),
                )
            },
        )
        assertTrue(result.isSuccess)
        verifySequence {
            firebase.setUserProperty("tier", null)
            firebase.setUserProperty("level", "expert")
        }
    }
}
