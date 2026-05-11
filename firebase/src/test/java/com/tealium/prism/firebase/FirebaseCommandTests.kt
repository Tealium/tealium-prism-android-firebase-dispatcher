package com.tealium.prism.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseCommandTests {

    @Test
    fun commandName_matches_spec_strings() {
        assertEquals("logevent", FirebaseCommand.LOG_EVENT.commandName)
        assertEquals("setuserid", FirebaseCommand.SET_USER_ID.commandName)
        assertEquals("setuserproperty", FirebaseCommand.SET_USER_PROPERTY.commandName)
        assertEquals("resetdata", FirebaseCommand.RESET_DATA.commandName)
        assertEquals("setdefaultparameters", FirebaseCommand.SET_DEFAULT_PARAMETERS.commandName)
        assertEquals("setconsent", FirebaseCommand.SET_CONSENT.commandName)
        assertEquals("setsessiontimeout", FirebaseCommand.SET_SESSION_TIMEOUT.commandName)
        assertEquals(
            "setanalyticscollectionenabled",
            FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED.commandName,
        )
    }
}
