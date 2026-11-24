package com.tealium.prism.firebase.internal.commands

import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
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

class SetConsentCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
    private val command = setConsentCommand(firebase)

    @Test
    fun forwards_known_consent_types_and_statuses() {
        val result = runCommand(
            command,
            consentPayload(
                "ad_storage" to "granted",
                "analytics_storage" to "denied",
            ),
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            firebase.setConsent(
                mapOf(
                    ConsentType.AD_STORAGE to ConsentStatus.GRANTED,
                    ConsentType.ANALYTICS_STORAGE to ConsentStatus.DENIED,
                ),
            )
        }
    }

    @Test
    fun all_four_known_consent_types_forwarded() {
        val result = runCommand(
            command,
            consentPayload(
                "ad_storage" to "granted",
                "analytics_storage" to "granted",
                "ad_user_data" to "denied",
                "ad_personalization" to "denied",
            ),
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            firebase.setConsent(
                mapOf(
                    ConsentType.AD_STORAGE to ConsentStatus.GRANTED,
                    ConsentType.ANALYTICS_STORAGE to ConsentStatus.GRANTED,
                    ConsentType.AD_USER_DATA to ConsentStatus.DENIED,
                    ConsentType.AD_PERSONALIZATION to ConsentStatus.DENIED,
                ),
            )
        }
    }

    @Test
    fun status_is_case_insensitive() {
        val result = runCommand(
            command,
            consentPayload(
                "ad_storage" to "GRANTED",
                "analytics_storage" to "Denied",
            ),
        )
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            firebase.setConsent(
                mapOf(
                    ConsentType.AD_STORAGE to ConsentStatus.GRANTED,
                    ConsentType.ANALYTICS_STORAGE to ConsentStatus.DENIED,
                ),
            )
        }
    }

    @Test
    fun status_is_trimmed() {
        val result = runCommand(command, consentPayload("ad_storage" to " granted\n"))
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            firebase.setConsent(mapOf(ConsentType.AD_STORAGE to ConsentStatus.GRANTED))
        }
    }

    @Test
    fun type_is_case_insensitive() {
        val result = runCommand(command, consentPayload("AD_STORAGE" to "granted"))
        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            firebase.setConsent(mapOf(ConsentType.AD_STORAGE to ConsentStatus.GRANTED))
        }
    }

    @Test
    fun unknown_consent_type_fails_with_invalid_value() {
        val result = runCommand(
            command,
            consentPayload(
                "ad_storage" to "granted",
                "future_flag" to "granted",
            ),
        )

        val error = result.assertFailure<ExtractionException.InvalidValue>()
        assertEquals("consent_settings.future_flag", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun unknown_consent_status_fails_with_invalid_value() {
        val result = runCommand(command, consentPayload("ad_storage" to "maybe"))

        val error = result.assertFailure<ExtractionException.InvalidValue>()
        assertEquals("consent_settings.ad_storage", error.key)
        assertEquals("expected one of denied, granted", error.reason)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun non_string_status_value_fails_with_invalid_value() {
        val result = runCommand(command, consentPayload("ad_storage" to 42))

        val error = result.assertFailure<ExtractionException.InvalidValue>()
        assertEquals("consent_settings.ad_storage", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun blank_status_value_fails_with_empty() {
        val result = runCommand(command, consentPayload("ad_storage" to " "))

        val error = result.assertFailure<ExtractionException.Empty>()
        assertEquals("consent_settings.ad_storage", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun null_status_value_fails_with_missing() {
        val result = runCommand(command, consentPayload("ad_storage" to DataItem.NULL))

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("consent_settings.ad_storage", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun empty_consent_dict_fails_with_empty() {
        val result = runCommand(command, consentPayload())

        val error = result.assertFailure<ExtractionException.Empty>()
        assertEquals("consent_settings", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun non_object_settings_fails_with_type_mismatch() {
        val result = runCommand(command, DataObject.create { put("consent_settings", "granted") })

        val error = result.assertFailure<ExtractionException.TypeMismatch>()
        assertEquals("consent_settings", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    @Test
    fun missing_settings_fails_with_missing() {
        val result = runCommand(command, DataObject.create {})

        val error = result.assertFailure<ExtractionException.Missing>()
        assertEquals("consent_settings", error.key)
        verify(exactly = 0) { firebase.setConsent(any()) }
    }

    private fun consentPayload(vararg settings: Pair<String, Any>): DataObject =
        DataObject.create {
            put("consent_settings", DataObject.create {
                for ((type, status) in settings) {
                    put(type, DataItem.convert(status))
                }
            })
        }
}
