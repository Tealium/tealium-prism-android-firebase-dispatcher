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
    fun missing_parameter_fails_with_missing() {
        val result = runCommand(command, DataObject.create {})

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("analytics_collection_enabled", error.key)
        verify(exactly = 0) { firebase.setAnalyticsCollectionEnabled(any()) }
    }

    @Test
    fun null_value_fails_with_missing() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", DataItem.NULL) })

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("analytics_collection_enabled", error.key)
        verify(exactly = 0) { firebase.setAnalyticsCollectionEnabled(any()) }
    }

    @Test
    fun non_boolean_value_fails_with_type_mismatch() {
        val result = runCommand(command, DataObject.create { put("analytics_collection_enabled", "maybe") })

        val error = result.assertFailure<ExtractionException.TypeMismatch>()
        assertEquals("analytics_collection_enabled", error.key)
        verify(exactly = 0) { firebase.setAnalyticsCollectionEnabled(any()) }
    }
}
