package com.tealium.prism.firebase.internal

import com.tealium.prism.core.api.data.DataItemUtils.asDataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.misc.Callback
import com.tealium.prism.core.api.misc.Scheduler
import com.tealium.prism.core.api.tracking.Dispatch
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.NoOpLogger
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebaseDispatcherTests {

    private val firebase = MockFirebaseAnalytics()

    private fun newDispatcher(
        configuration: FirebaseDispatcherConfiguration =
            FirebaseDispatcherConfiguration(null, null, null),
    ): FirebaseDispatcher = FirebaseDispatcher(
        firebaseInstance = firebase,
        configuration = configuration,
        logger = NoOpLogger(),
        scheduler = Scheduler.SYNCHRONOUS,
    )

    @Test
    fun init_applies_session_timeout_and_analytics_enabled() {
        newDispatcher(FirebaseDispatcherConfiguration(1800.0, true, null))
        assertEquals(1_800_000L, firebase.lastSessionTimeoutMillis)
        assertEquals(true, firebase.lastAnalyticsEnabled)
    }

    @Test
    fun init_applies_all_configuration_settings() {
        newDispatcher(FirebaseDispatcherConfiguration(900.0, false, null))
        assertEquals(900_000L, firebase.lastSessionTimeoutMillis)
        assertEquals(false, firebase.lastAnalyticsEnabled)
    }

    @Test
    fun init_with_nulls_does_not_touch_firebase() {
        newDispatcher()
        assertEquals(0, firebase.setSessionTimeoutCount)
        assertEquals(0, firebase.setAnalyticsEnabledCount)
    }

    @Test
    fun updateConfiguration_applies_new_values() {
        val dispatcher = newDispatcher()
        dispatcher.updateConfiguration(
            DataObject.create {
                put(FirebaseDispatcherConfiguration.KEY_SESSION_TIMEOUT, 900.0)
                put(FirebaseDispatcherConfiguration.KEY_ANALYTICS_ENABLED, false)
            }
        )
        assertEquals(900_000L, firebase.lastSessionTimeoutMillis)
        assertEquals(false, firebase.lastAnalyticsEnabled)
    }


    @Test
    fun dispatch_routes_single_command_to_firebase() {
        val dispatcher = newDispatcher()
        val dispatch = Dispatch.create(
            "purchase",
            dataObject = DataObject.create {
                put(Dispatch.Keys.COMMAND_NAME, FirebaseCommand.LOG_EVENT.commandName)
                put("event_name", "purchase")
            }
        )
        val callback = mockk<Callback<List<Dispatch>>>(relaxed = true)

        dispatcher.dispatch(listOf(dispatch), callback)

        assertEquals(1, firebase.loggedEvents.size)
        assertEquals("purchase", firebase.loggedEvents[0].name)
        verify(exactly = 1) { callback.onComplete(listOf(dispatch)) }
    }

    @Test
    fun dispatch_with_empty_command_array_still_completes() {
        val dispatcher = newDispatcher()
        val dispatch = Dispatch.create(
            "empty",
            dataObject = DataObject.create {
                put(Dispatch.Keys.COMMAND_NAME, emptyList<String>().asDataList())
            },
        )
        val callback = mockk<Callback<List<Dispatch>>>(relaxed = true)

        dispatcher.dispatch(listOf(dispatch), callback)

        assertEquals(0, firebase.loggedEvents.size)
        verify(exactly = 1) { callback.onComplete(listOf(dispatch)) }
    }

    @Test
    fun dispatch_with_mixed_valid_invalid_commands_executes_valid_only() {
        val dispatcher = newDispatcher()
        val dispatch = Dispatch.create(
            "mixed",
            dataObject = DataObject.create {
                put(
                    Dispatch.Keys.COMMAND_NAME,
                    listOf("invalid_command", FirebaseCommand.LOG_EVENT.commandName).asDataList(),
                )
                put("event_name", "test_event")
            },
        )
        val callback = mockk<Callback<List<Dispatch>>>(relaxed = true)

        dispatcher.dispatch(listOf(dispatch), callback)

        assertEquals(1, firebase.loggedEvents.size)
        verify(exactly = 1) { callback.onComplete(listOf(dispatch)) }
    }

    @Test
    fun dispatch_routes_multi_command_payload() {
        val dispatcher = newDispatcher()
        val dispatch = Dispatch.create(
            "login",
            dataObject = DataObject.create {
                put(
                    Dispatch.Keys.COMMAND_NAME,
                    listOf(
                        FirebaseCommand.LOG_EVENT.commandName,
                        FirebaseCommand.SET_USER_ID.commandName,
                    ).asDataList(),
                )
                put("event_name", "login")
                put("user_id", "user_123")
            }
        )
        val callback = mockk<Callback<List<Dispatch>>>(relaxed = true)

        dispatcher.dispatch(listOf(dispatch), callback)

        assertEquals(1, firebase.loggedEvents.size)
        assertEquals("user_123", firebase.lastUserId)
        verify(exactly = 1) { callback.onComplete(listOf(dispatch)) }
    }

    @Test
    fun dispatch_without_command_key_still_completes() {
        val dispatcher = newDispatcher()
        val dispatch = Dispatch.create(
            "event",
            dataObject = DataObject.create { put("event_name", "ignored") },
        )
        val callback = mockk<Callback<List<Dispatch>>>(relaxed = true)

        dispatcher.dispatch(listOf(dispatch), callback)

        assertEquals(0, firebase.loggedEvents.size)
        verify(exactly = 1) { callback.onComplete(listOf(dispatch)) }
    }

    @Test
    fun dispatch_with_unknown_command_does_not_invoke_firebase() {
        val dispatcher = newDispatcher()
        val dispatch = Dispatch.create(
            "unknown",
            dataObject = DataObject.create {
                put(Dispatch.Keys.COMMAND_NAME, "not_a_real_command")
            },
        )
        val callback = mockk<Callback<List<Dispatch>>>(relaxed = true)

        dispatcher.dispatch(listOf(dispatch), callback)

        assertEquals(0, firebase.loggedEvents.size)
        assertEquals(0, firebase.setUserIdCount)
        verify(exactly = 1) { callback.onComplete(listOf(dispatch)) }
    }
}
