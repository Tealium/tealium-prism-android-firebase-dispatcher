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

class SetAnalyticsCollectionEnabledCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
    private val command = setAnalyticsCollectionEnabledCommand(firebase)

    @Test
    fun forwards_true() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", true) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setAnalyticsCollectionEnabled(true) }
    }

    @Test
    fun forwards_false() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", false) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.setAnalyticsCollectionEnabled(false) }
    }

    @Test
    fun missing_parameter_fails_with_missing_parameter() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("missing"))
        verify(exactly = 0) { firebase.setAnalyticsCollectionEnabled(any()) }
    }

    @Test
    fun non_boolean_value_fails_with_invalid_parameter_type() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", "maybe") })
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("expected type"))
        verify(exactly = 0) { firebase.setAnalyticsCollectionEnabled(any()) }
    }
}
