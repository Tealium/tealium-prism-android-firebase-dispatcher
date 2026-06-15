package com.tealium.prism.firebase.internal.commands

import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetConsentCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = setConsentCommand(firebase)

    @Test
    fun forwards_known_consent_types_and_statuses() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "consent_settings",
                    DataObject.create {
                        put("ad_storage", "granted")
                        put("analytics_storage", "denied")
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val settings = firebase.lastConsentSettings!!
        assertEquals(ConsentStatus.GRANTED, settings[ConsentType.AD_STORAGE])
        assertEquals(ConsentStatus.DENIED, settings[ConsentType.ANALYTICS_STORAGE])
    }

    @Test
    fun all_four_known_consent_types_forwarded() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "consent_settings",
                    DataObject.create {
                        put("ad_storage", "granted")
                        put("analytics_storage", "granted")
                        put("ad_user_data", "denied")
                        put("ad_personalization", "denied")
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val settings = firebase.lastConsentSettings!!
        assertEquals(4, settings.size)
        assertEquals(ConsentStatus.GRANTED, settings[ConsentType.AD_STORAGE])
        assertEquals(ConsentStatus.GRANTED, settings[ConsentType.ANALYTICS_STORAGE])
        assertEquals(ConsentStatus.DENIED, settings[ConsentType.AD_USER_DATA])
        assertEquals(ConsentStatus.DENIED, settings[ConsentType.AD_PERSONALIZATION])
    }

    @Test
    fun unknown_consent_type_throws() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "consent_settings",
                    DataObject.create {
                        put("ad_storage", "granted")
                        put("future_flag", "granted")
                    },
                )
            },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        assertNull(firebase.lastConsentSettings)
    }

    @Test
    fun unknown_consent_status_throws() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "consent_settings",
                    DataObject.create {
                        put("ad_storage", "maybe")
                    },
                )
            },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        assertNull(firebase.lastConsentSettings)
    }

    @Test
    fun status_is_case_insensitive() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "consent_settings",
                    DataObject.create {
                        put("ad_storage", "GRANTED")
                        put("analytics_storage", "Denied")
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val settings = firebase.lastConsentSettings!!
        assertEquals(ConsentStatus.GRANTED, settings[ConsentType.AD_STORAGE])
        assertEquals(ConsentStatus.DENIED, settings[ConsentType.ANALYTICS_STORAGE])
    }

    @Test
    fun non_string_status_value_throws() {
        val result = runCommand(
            command,
            DataObject.create {
                put(
                    "consent_settings",
                    DataObject.create {
                        put("ad_storage", 42)
                    },
                )
            },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
    }

    @Test
    fun empty_consent_dict_fails() {
        val result = runCommand(
            command,
            DataObject.create { put("consent_settings", DataObject.create {}) },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
    }

    @Test
    fun missing_settings_fails() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
    }
}
