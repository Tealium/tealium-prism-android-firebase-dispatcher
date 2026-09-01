package com.tealium.prism.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseDestinationTests {

    @Test
    fun event_destinations_match_spec_paths() {
        assertEquals("event_name", FirebaseDestination.EventName.asJsonObjectPath().toString())
        assertEquals("parameters", FirebaseDestination.EventParams.asJsonObjectPath().toString())
        assertEquals(
            "parameters.value",
            FirebaseDestination.EventParam(FirebaseAnalytics.Param.VALUE).asJsonObjectPath().toString(),
        )
        assertEquals(
            "parameters.items.item_id",
            FirebaseDestination.ItemParam(FirebaseAnalytics.Param.ITEM_ID).asJsonObjectPath().toString(),
        )
    }

    @Test
    fun user_destinations_match_spec_paths() {
        assertEquals("user_id", FirebaseDestination.UserId.asJsonObjectPath().toString())
        assertEquals("property_name", FirebaseDestination.UserPropertyName.asJsonObjectPath().toString())
        assertEquals("property_value", FirebaseDestination.UserPropertyValue.asJsonObjectPath().toString())
    }

    @Test
    fun default_params_destinations_match_spec_paths() {
        assertEquals("parameters", FirebaseDestination.DefaultParams.asJsonObjectPath().toString())
        assertEquals(
            "parameters.app_version",
            FirebaseDestination.DefaultParam("app_version").asJsonObjectPath().toString(),
        )
    }

    @Test
    fun consent_destinations_use_string_raw_values() {
        assertEquals(
            "consent_settings",
            FirebaseDestination.ConsentSettings.asJsonObjectPath().toString(),
        )
        assertEquals(
            "consent_settings.ad_storage",
            FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.AD_STORAGE)
                .asJsonObjectPath().toString(),
        )
        assertEquals(
            "consent_settings.analytics_storage",
            FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE)
                .asJsonObjectPath().toString(),
        )
        assertEquals(
            "consent_settings.ad_user_data",
            FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.AD_USER_DATA)
                .asJsonObjectPath().toString(),
        )
        assertEquals(
            "consent_settings.ad_personalization",
            FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.AD_PERSONALIZATION)
                .asJsonObjectPath().toString(),
        )
    }

    @Test
    fun config_destinations_match_spec_paths() {
        assertEquals(
            "session_timeout_seconds",
            FirebaseDestination.SessionTimeout.asJsonObjectPath().toString(),
        )
        assertEquals(
            "analytics_collection_enabled",
            FirebaseDestination.AnalyticsEnabled.asJsonObjectPath().toString(),
        )
    }
}
