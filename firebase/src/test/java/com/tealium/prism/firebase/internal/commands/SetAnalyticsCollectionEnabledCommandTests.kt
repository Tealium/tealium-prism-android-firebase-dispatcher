package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetAnalyticsCollectionEnabledCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = setAnalyticsCollectionEnabledCommand(firebase)

    @Test
    fun forwards_true() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", true) })
        assertTrue(result.isSuccess)
        assertEquals(true, firebase.lastAnalyticsEnabled)
    }

    @Test
    fun forwards_false() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", false) })
        assertTrue(result.isSuccess)
        assertEquals(false, firebase.lastAnalyticsEnabled)
    }

    @Test
    fun missing_parameter_fails_with_missing_parameter() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("missing"))
        assertEquals(0, firebase.setAnalyticsEnabledCount)
    }

    @Test
    fun non_boolean_value_fails_with_invalid_parameter_type() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", "maybe") })
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("expected type"))
        assertEquals(0, firebase.setAnalyticsEnabledCount)
    }
}
