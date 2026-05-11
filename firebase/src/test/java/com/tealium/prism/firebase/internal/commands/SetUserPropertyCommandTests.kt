package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetUserPropertyCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = SetUserPropertyCommand(firebase)

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
        val prop = firebase.userProperties.single()
        assertEquals("tier", prop.name)
        assertEquals("premium", prop.value)
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
        val prop = firebase.userProperties.single()
        assertEquals("tier", prop.name)
        assertNull(prop.value)
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
        assertEquals(2, firebase.userProperties.size)
        assertEquals(
            listOf("tier" to "premium", "level" to "expert"),
            firebase.userProperties.map { it.name to it.value },
        )
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
        assertTrue(firebase.userProperties.isEmpty())
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
        val prop = firebase.userProperties.single()
        assertEquals("42", prop.name)
        assertEquals("99", prop.value)
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
        val prop = firebase.userProperties.single()
        assertEquals("score", prop.name)
        assertEquals("9.5", prop.value)
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
        assertTrue(result.exceptionOrNull() is CommandException)
        assertTrue(firebase.userProperties.isEmpty())
    }

    @Test
    fun missing_value_fails() {
        val result = runCommand(
            command,
            DataObject.create { put("property_name", "tier") },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        assertTrue(firebase.userProperties.isEmpty())
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
        assertTrue(firebase.userProperties.isEmpty())
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
        assertEquals(2, firebase.userProperties.size)
        assertEquals("tier", firebase.userProperties[0].name)
        assertNull(firebase.userProperties[0].value)
        assertEquals("level", firebase.userProperties[1].name)
        assertEquals("expert", firebase.userProperties[1].value)
    }
}
