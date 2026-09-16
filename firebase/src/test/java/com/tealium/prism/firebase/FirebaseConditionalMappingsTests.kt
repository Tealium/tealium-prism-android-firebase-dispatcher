package com.tealium.prism.firebase

import com.tealium.prism.core.api.data.JsonPath
import com.tealium.prism.core.api.settings.MappingParameters
import com.tealium.prism.core.api.settings.json.TransformationOperation
import com.tealium.prism.core.api.tracking.Dispatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for conditional mappings using ifValueEquals.
 *
 * Mirrors iOS `FirebaseConditionalMappingsTests`.
 */
class FirebaseConditionalMappingsTests {

    private fun buildMappings(
        block: FirebaseMappings.() -> Unit,
    ): List<TransformationOperation<MappingParameters>> {
        val settings = FirebaseSettingsBuilder()
            .setMappings(block)
            .build()
        val converter = TransformationOperation.Converter(MappingParameters.Converter)
        return settings.getDataList(KEY_MAPPINGS)!!.mapNotNull { converter.convert(it) }
    }

    private fun TransformationOperation<MappingParameters>.filterValue() =
        parameters.filter?.value

    private fun TransformationOperation<MappingParameters>.sourcePath() =
        parameters.reference?.path

    private fun TransformationOperation<MappingParameters>.destinationPath() =
        destination.path

    @Test
    fun conditional_command_with_ifValueEquals_on_event_key_sets_filter_and_source() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.LOG_EVENT)
                .ifValueEquals(Dispatch.Keys.TEALIUM_EVENT, "screen_view")
        }
        val op = mappings.single()
        assertEquals(JsonPath[Dispatch.Keys.TEALIUM_EVENT], op.sourcePath())
        assertEquals("screen_view", op.filterValue())
        assertEquals("logevent", op.parameters.mapTo?.value?.value)
    }

    @Test
    fun conditional_eventName_with_ifValueEquals_sets_filter_on_source() {
        val mappings = buildMappings {
            mapFrom(Dispatch.Keys.TEALIUM_EVENT, FirebaseDestination.EventName)
                .ifValueEquals("screen_view")
        }
        val op = mappings.single()
        assertEquals(JsonPath[Dispatch.Keys.TEALIUM_EVENT], op.sourcePath())
        assertEquals("screen_view", op.filterValue())
        assertEquals(JsonPath["event_name"], op.destinationPath())
    }

    @Test
    fun unconditional_mapping_has_no_filter() {
        val mappings = buildMappings {
            mapFrom(Dispatch.Keys.TEALIUM_EVENT, FirebaseDestination.EventName)
        }
        val op = mappings.single()
        assertNull(op.filterValue())
    }

    @Test
    fun multiple_commands_produce_multiple_operations() {
        val mappings = buildMappings {
            mapCommand(FirebaseCommand.LOG_EVENT)
            mapFrom(Dispatch.Keys.TEALIUM_EVENT, FirebaseDestination.EventName)
            mapCommand(FirebaseCommand.SET_USER_ID)
            mapFrom("customer_id", FirebaseDestination.UserId)
        }
        assertEquals(4, mappings.size)
        assertEquals("logevent", mappings[0].parameters.mapTo?.value?.value)
        assertEquals("setuserid", mappings[2].parameters.mapTo?.value?.value)
    }

    @Test
    fun keep_produces_operation_with_same_source_and_destination() {
        val mappings = buildMappings { keep(FirebaseDestination.UserId) }
        val op = mappings.single()
        assertNotNull(op.sourcePath())
        assertEquals(op.destinationPath(), op.sourcePath())
        assertNull(op.filterValue())
    }

    private companion object {
        const val KEY_MAPPINGS = "mappings"
    }
}
