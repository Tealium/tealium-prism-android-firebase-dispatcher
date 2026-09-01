package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetUserIdCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
    private val command = setUserIdCommand(firebase)

    @Test
    fun forwards_string_to_firebase() {
        val result = runCommand(command, DataObject.create { put("user_id", "abc") })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserId("abc") }
    }

    @Test
    fun empty_string_clears_user_id() {
        val result = runCommand(command, DataObject.create { put("user_id", "") })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserId(null) }
    }

    @Test
    fun missing_parameter_fails_with_missing_parameter() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("missing"))
        verify(exactly = 0) { firebase.setUserId(any()) }
    }

    @Test
    fun non_string_value_fails_with_invalid_parameter_type() {
        val result = runCommand(
            command,
            DataObject.create { put("user_id", DataObject.create { put("nested", "value") }) },
        )
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("expected type"))
        verify(exactly = 0) { firebase.setUserId(any()) }
    }

    @Test
    fun coerces_integer_user_id_to_string() {
        val result = runCommand(command, DataObject.create { put("user_id", 12345) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserId("12345") }
    }

    @Test
    fun coerces_long_user_id_to_string() {
        val result = runCommand(command, DataObject.create { put("user_id", 123L) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setUserId("123") }
    }
}
