package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetDataCommandTests {

    @Test
    fun invokes_firebase_reset() {
        val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
        val command = resetDataCommand(firebase)
        val result = runCommand(command, DataObject.create {})
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.resetAnalyticsData() }
    }
}
