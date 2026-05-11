package com.tealium.prism.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.data.JsonPath
import com.tealium.prism.core.api.settings.MappingParameters
import com.tealium.prism.core.api.settings.json.TransformationOperation
import com.tealium.prism.core.api.tracking.Dispatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Parity tests for Firebase mapping destinations — verifies that each
 * [FirebaseDestination] and [FirebaseCommand] produces the expected
 * payload paths after the settings builder serialises them.
 *
 * Mirrors iOS `FirebaseCommandMappingsTests`.
 */
class FirebaseCommandMappingsTests {

    private fun buildMappings(
        block: FirebaseMappings.() -> Unit,
    ): List<TransformationOperation<MappingParameters>> {
        val settings = FirebaseSettingsBuilder()
            .setMappings(block)
            .build()
        val converter = TransformationOperation.Converter(MappingParameters.Converter)
        return settings.getDataList(KEY_MAPPINGS)!!.mapNotNull { converter.convert(it) }
    }

    private fun TransformationOperation<MappingParameters>.sourcePath() =
        parameters.reference?.path

    private fun TransformationOperation<MappingParameters>.destinationPath() =
        destination.path

    @Test
    fun logEvent_command_maps_command_name() {
        val mappings = buildMappings { mapCommand(FirebaseCommand.LOG_EVENT) }
        val op = mappings.single()
        assertEquals(JsonPath[Dispatch.Keys.COMMAND_NAME], op.destinationPath())
        assertEquals("logevent", op.parameters.mapTo?.value?.getString())
    }

    @Test
    fun logEvent_maps_event_name_source_to_event_name_destination() {
        val mappings = buildMappings {
            mapFrom("tealium_event", FirebaseDestination.EventName)
        }
        val op = mappings.single()
        assertEquals(JsonPath["tealium_event"], op.sourcePath())
        assertEquals(JsonPath["event_name"], op.destinationPath())
    }

    @Test
    fun logEvent_maps_event_param_under_parameters() {
        val mappings = buildMappings {
            mapFrom(
                "total",
                FirebaseDestination.EventParam(FirebaseAnalytics.Param.VALUE),
            )
        }
        val op = mappings.single()
        assertEquals(JsonPath["total"], op.sourcePath())
        assertEquals(
            JsonPath["parameters"].key(FirebaseAnalytics.Param.VALUE),
            op.destinationPath(),
        )
    }

    @Test
    fun logEvent_maps_item_param_under_parameters_items() {
        val mappings = buildMappings {
            mapFrom(
                "product_ids",
                FirebaseDestination.ItemParam(FirebaseAnalytics.Param.ITEM_ID),
            )
        }
        val op = mappings.single()
        assertEquals(JsonPath["product_ids"], op.sourcePath())
        assertEquals(
            JsonPath["parameters"]
                .key(FirebaseAnalytics.Param.ITEMS)
                .key(FirebaseAnalytics.Param.ITEM_ID),
            op.destinationPath(),
        )
    }

    @Test
    fun logEvent_maps_event_params_bulk_destination() {
        val mappings = buildMappings {
            mapFrom("event_params", FirebaseDestination.EventParams)
        }
        val op = mappings.single()
        assertEquals(JsonPath["event_params"], op.sourcePath())
        assertEquals(JsonPath["parameters"], op.destinationPath())
    }

    @Test
    fun setUserId_maps_source_to_user_id() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_USER_ID)
            mapFrom("customer_id", FirebaseDestination.UserId)
        }
        assertEquals(2, mappings.size)
        val cmdOp = mappings[0]
        assertEquals("setuserid", cmdOp.parameters.mapTo?.value?.getString())
        val fromOp = mappings[1]
        assertEquals(JsonPath["customer_id"], fromOp.sourcePath())
        assertEquals(JsonPath["user_id"], fromOp.destinationPath())
    }

    @Test
    fun setUserProperty_maps_single_scalar_name_and_value() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_USER_PROPERTY)
            mapFrom("prop_name", FirebaseDestination.UserPropertyName)
            mapFrom("prop_value", FirebaseDestination.UserPropertyValue)
        }
        assertEquals(3, mappings.size)
        assertEquals(JsonPath["prop_name"], mappings[1].sourcePath())
        assertEquals(JsonPath["property_name"], mappings[1].destinationPath())
        assertEquals(JsonPath["prop_value"], mappings[2].sourcePath())
        assertEquals(JsonPath["property_value"], mappings[2].destinationPath())
    }

    @Test
    fun setDefaultParameters_individual_and_bulk_destinations() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_DEFAULT_PARAMETERS)
            mapFrom("app_version", FirebaseDestination.DefaultParam("app_version"))
            mapFrom("default_params", FirebaseDestination.DefaultParams)
        }
        assertEquals(3, mappings.size)
        assertEquals(
            JsonPath["parameters"].key("app_version"),
            mappings[1].destinationPath(),
        )
        assertEquals(JsonPath["parameters"], mappings[2].destinationPath())
    }

    @Test
    fun setConsent_maps_individual_consent_settings() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_CONSENT)
            mapFrom(
                "analytics",
                FirebaseDestination.ConsentSetting(
                    FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE,
                ),
            )
            mapFrom(
                "ad",
                FirebaseDestination.ConsentSetting(
                    FirebaseAnalytics.ConsentType.AD_STORAGE,
                ),
            )
        }
        assertEquals(3, mappings.size)
        assertEquals(
            JsonPath["consent_settings"].key("analytics_storage"),
            mappings[1].destinationPath(),
        )
        assertEquals(
            JsonPath["consent_settings"].key("ad_storage"),
            mappings[2].destinationPath(),
        )
    }

    @Test
    fun setConsent_maps_all_four_consent_types() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_CONSENT)
            mapFrom("analytics", FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE))
            mapFrom("ad", FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.AD_STORAGE))
            mapFrom("ad_user", FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.AD_USER_DATA))
            mapFrom("ad_personalization", FirebaseDestination.ConsentSetting(FirebaseAnalytics.ConsentType.AD_PERSONALIZATION))
        }
        assertEquals(5, mappings.size)
        assertEquals(JsonPath["consent_settings"].key("analytics_storage"), mappings[1].destinationPath())
        assertEquals(JsonPath["consent_settings"].key("ad_storage"), mappings[2].destinationPath())
        assertEquals(JsonPath["consent_settings"].key("ad_user_data"), mappings[3].destinationPath())
        assertEquals(JsonPath["consent_settings"].key("ad_personalization"), mappings[4].destinationPath())
    }

    @Test
    fun setConsent_bulk_destination() {
        val mappings = buildMappings {
            mapFrom("consent", FirebaseDestination.ConsentSettings)
        }
        val op = mappings.single()
        assertEquals(JsonPath["consent_settings"], op.destinationPath())
    }

    @Test
    fun setSessionTimeout_maps_to_session_timeout_seconds() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_SESSION_TIMEOUT)
            mapFrom("timeout", FirebaseDestination.SessionTimeout)
        }
        val op = mappings[1]
        assertEquals(JsonPath["timeout"], op.sourcePath())
        assertEquals(JsonPath["session_timeout_seconds"], op.destinationPath())
    }

    @Test
    fun setAnalyticsCollectionEnabled_maps_to_analytics_collection_enabled() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED)
            mapFrom("enabled", FirebaseDestination.AnalyticsEnabled)
        }
        val op = mappings[1]
        assertEquals(JsonPath["enabled"], op.sourcePath())
        assertEquals(JsonPath["analytics_collection_enabled"], op.destinationPath())
    }

    @Test
    fun resetData_only_command_mapping() {
        val mappings = buildMappings { mapCommand(FirebaseCommand.RESET_DATA) }
        val op = mappings.single()
        assertEquals(JsonPath[Dispatch.Keys.COMMAND_NAME], op.destinationPath())
        assertEquals("resetdata", op.parameters.mapTo?.value?.getString())
    }

    @Test
    fun keep_preserves_destination_as_source() {
        val mappings = buildMappings { keep(FirebaseDestination.UserId) }
        val op = mappings.single()
        assertNotNull(op.sourcePath())
        assertEquals(op.destinationPath(), op.sourcePath())
    }

    private companion object {
        const val KEY_MAPPINGS = "mappings"
    }
}
