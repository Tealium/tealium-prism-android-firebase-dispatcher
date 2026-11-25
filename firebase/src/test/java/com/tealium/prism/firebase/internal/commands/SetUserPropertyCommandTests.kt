package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataItem
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.data.ExtractionException
import com.tealium.prism.firebase.helpers.assertFailure
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
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

        val error = result.assertFailure<CommandException>()
        assertEquals(
            CommandException.arrayLengthMismatch("property_name", "property_value").message,
            error.message,
        )
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun array_length_mismatch_is_reported_before_null_names() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.create { add(DataItem.NULL) })
                put("property_value", DataList.fromStringCollection(listOf("value_a", "value_b")))
            },
        )

        val error = result.assertFailure<CommandException>()
        assertEquals(
            CommandException.arrayLengthMismatch("property_name", "property_value").message,
            error.message,
        )
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun missing_name_fails_with_missing() {
        val result = runCommand(
            command,
            DataObject.create { put("property_value", "premium") },
        )

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("property_name", error.key)
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
    fun coerces_numeric_array_entries_to_strings() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.create { add(42); add("tier") })
                put("property_value", DataList.create { add(1); add(9.5) })
            },
        )
        assertTrue(result.isSuccess)
        verifySequence {
            firebase.setUserProperty("42", "1")
            firebase.setUserProperty("tier", "9.5")
        }
    }

    @Test
    fun skips_array_names_that_cannot_be_read_as_string() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "property_name",
                    DataList.create {
                        add(DataObject.create { put("nested", "object") })
                        add("tier")
                    },
                )
                put("property_value", DataList.fromStringCollection(listOf("ignored", "premium")))
            },
        )
        assertTrue(result.isSuccess)
        verifySequence { firebase.setUserProperty("tier", "premium") }
    }

    @Test
    fun all_null_names_array_fails_with_empty() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.fromStringCollection(listOf<String?>(null, null)))
                put("property_value", DataList.fromStringCollection(listOf("value_a", "value_b")))
            },
        )

        val error = result.assertFailure<ExtractionException.Empty>()
        assertEquals("property_name", error.key)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun empty_names_array_fails_with_empty() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataList.EMPTY_LIST)
                put("property_value", DataList.EMPTY_LIST)
            },
        )

        val error = result.assertFailure<ExtractionException.Empty>()
        assertEquals("property_name", error.key)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun missing_value_fails_with_missing() {
        val result = runCommand(
            command,
            DataObject.create { put("property_name", "tier") },
        )

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("property_value", error.key)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun non_string_name_fails_with_type_mismatch() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataObject.create { put("nested", "object") })
                put("property_value", "premium")
            },
        )

        val error = result.assertFailure<ExtractionException.TypeMismatch>()
        assertEquals("property_name", error.key)
        assertEquals("String or array of String", error.expectedType)
        verify(exactly = 0) { firebase.setUserProperty(any(), any()) }
    }

    @Test
    fun null_name_fails_with_empty() {
        val result = runCommand(
            command,
            DataObject.create {
                put("property_name", DataItem.NULL)
                put("property_value", "premium")
            },
        )

        val error = result.assertFailure<ExtractionException.Empty>()
        assertEquals("property_name", error.key)
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
