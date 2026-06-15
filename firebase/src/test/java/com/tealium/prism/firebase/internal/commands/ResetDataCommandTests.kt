package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetDataCommandTests {

    @Test
    fun invokes_firebase_reset() {
        val firebase = MockFirebaseAnalytics()
        val command = resetDataCommand(firebase)
        val result = runCommand(command, DataObject.create {})
        assertTrue(result.isSuccess)
        assertEquals(1, firebase.resetAnalyticsDataCount)
    }
}
