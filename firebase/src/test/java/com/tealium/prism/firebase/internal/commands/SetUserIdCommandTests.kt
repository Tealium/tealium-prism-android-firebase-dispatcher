package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetUserIdCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = SetUserIdCommand(firebase)

    @Test
    fun forwards_string_to_firebase() {
        val result = runCommand(command, DataObject.create { put("user_id", "abc") })
        assertTrue(result.isSuccess)
        assertEquals(1, firebase.setUserIdCount)
        assertEquals("abc", firebase.lastUserId)
    }

    @Test
    fun empty_string_clears_user_id() {
        val result = runCommand(command, DataObject.create { put("user_id", "") })
        assertTrue(result.isSuccess)
        assertNull(firebase.lastUserId)
        assertEquals(1, firebase.setUserIdCount)
    }

    @Test
    fun missing_parameter_fails() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        assertEquals(0, firebase.setUserIdCount)
    }

    @Test
    fun coerces_integer_user_id_to_string() {
        val result = runCommand(command, DataObject.create { put("user_id", 12345) })
        assertTrue(result.isSuccess)
        assertEquals("12345", firebase.lastUserId)
    }

    @Test
    fun coerces_long_user_id_to_string() {
        val result = runCommand(command, DataObject.create { put("user_id", 123L) })
        assertTrue(result.isSuccess)
        assertEquals("123", firebase.lastUserId)
    }
}
