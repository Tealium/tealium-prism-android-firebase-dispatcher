package com.tealium.prism.firebase.internal.commands

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.runCommand
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogEventCommandTests {

    private val firebase = mockk<FirebaseAnalyticsInterface>(relaxed = true)
    private val command = logEventCommand(firebase)

    @Test
    fun forwards_event_without_parameters() {
        val result = runCommand(command, DataObject.create { put("event_name", "login") })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.logEvent("login", null) }
    }

    @Test
    fun forwards_event_with_scalar_parameters() {
        val result = runCommand(
            command,
            DataObject.create {
                put("event_name", "purchase")
                put(
                    "parameters",
                    DataObject.create {
                        put("value", 99.99)
                        put("currency", "USD")
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val params = slot<Bundle>()
        verify(exactly = 1) { firebase.logEvent("purchase", capture(params)) }
        assertEquals(99.99, params.captured.getDouble("value"), 0.0)
        assertEquals("USD", params.captured.getString("currency"))
    }

    @Test
    fun forwards_event_with_boolean_parameter() {
        val result = runCommand(
            command,
            DataObject.create {
                put("event_name", "flag_event")
                put(
                    "parameters",
                    DataObject.create { put("is_premium", true) },
                )
            },
        )
        assertTrue(result.isSuccess)
        val params = slot<Bundle>()
        verify(exactly = 1) { firebase.logEvent("flag_event", capture(params)) }
        assertEquals(true, params.captured.getBoolean("is_premium"))
    }

    @Test
    fun logs_event_with_items_parallel_arrays() {
        val result = runCommand(
            command,
            DataObject.create {
                put("event_name", "purchase")
                put(
                    "parameters",
                    DataObject.create {
                        put("value", 99.99)
                        put("currency", "USD")
                        put(
                            FirebaseAnalytics.Param.ITEMS,
                            DataObject.create {
                                put(
                                    FirebaseAnalytics.Param.ITEM_ID,
                                    DataList.fromStringCollection(listOf("SKU1", "SKU2")),
                                )
                                put(
                                    FirebaseAnalytics.Param.ITEM_NAME,
                                    DataList.fromStringCollection(listOf("Widget", "Gadget")),
                                )
                                put(
                                    FirebaseAnalytics.Param.PRICE,
                                    DataList.fromDoubleCollection(listOf(29.99, 70.00)),
                                )
                            },
                        )
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val params = slot<Bundle>()
        verify(exactly = 1) { firebase.logEvent("purchase", capture(params)) }
        assertEquals(99.99, params.captured.getDouble("value"), 0.0)
        assertEquals("USD", params.captured.getString("currency"))
        val items = params.captured.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)!!
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString(FirebaseAnalytics.Param.ITEM_ID))
        assertEquals("Widget", items[0].getString(FirebaseAnalytics.Param.ITEM_NAME))
        assertEquals(29.99, items[0].getDouble(FirebaseAnalytics.Param.PRICE), 0.0)
        assertEquals("SKU2", items[1].getString(FirebaseAnalytics.Param.ITEM_ID))
        assertEquals(70.00, items[1].getDouble(FirebaseAnalytics.Param.PRICE), 0.0)
    }

    @Test
    fun logs_event_with_items_array_of_objects() {
        val result = runCommand(
            command,
            DataObject.create {
                put("event_name", "purchase")
                put(
                    "parameters",
                    DataObject.create {
                        put(
                            FirebaseAnalytics.Param.ITEMS,
                            DataList.create {
                                add(
                                    DataObject.create {
                                        put(FirebaseAnalytics.Param.ITEM_ID, "SKU1")
                                        put(FirebaseAnalytics.Param.ITEM_NAME, "Widget")
                                        put(FirebaseAnalytics.Param.PRICE, 29.99)
                                    },
                                )
                                add(
                                    DataObject.create {
                                        put(FirebaseAnalytics.Param.ITEM_ID, "SKU2")
                                        put(FirebaseAnalytics.Param.ITEM_NAME, "Gadget")
                                        put(FirebaseAnalytics.Param.PRICE, 70.00)
                                    },
                                )
                            },
                        )
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val params = slot<Bundle>()
        verify(exactly = 1) { firebase.logEvent("purchase", capture(params)) }
        val items = params.captured.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)!!
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString(FirebaseAnalytics.Param.ITEM_ID))
        assertEquals("SKU2", items[1].getString(FirebaseAnalytics.Param.ITEM_ID))
    }

    @Test
    fun logs_event_with_items_mixed_scalar_types() {
        val result = runCommand(
            command,
            DataObject.create {
                put("event_name", "add_to_cart")
                put(
                    "parameters",
                    DataObject.create {
                        put(
                            FirebaseAnalytics.Param.ITEMS,
                            DataObject.create {
                                put(
                                    FirebaseAnalytics.Param.ITEM_ID,
                                    DataList.fromStringCollection(listOf("SKU1", "SKU2")),
                                )
                                put(
                                    FirebaseAnalytics.Param.QUANTITY,
                                    DataList.fromIntCollection(listOf(1, 3)),
                                )
                                put(
                                    FirebaseAnalytics.Param.PRICE,
                                    DataList.fromDoubleCollection(listOf(29.99, 70.00)),
                                )
                            },
                        )
                    },
                )
            },
        )
        assertTrue(result.isSuccess)
        val params = slot<Bundle>()
        verify(exactly = 1) { firebase.logEvent("add_to_cart", capture(params)) }
        val items = params.captured.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)!!
        assertEquals(2, items.size)
        assertEquals(1, items[0].getInt(FirebaseAnalytics.Param.QUANTITY))
        assertEquals(3, items[1].getInt(FirebaseAnalytics.Param.QUANTITY))
        assertEquals(29.99, items[0].getDouble(FirebaseAnalytics.Param.PRICE), 0.0)
    }

    @Test
    fun mismatched_item_array_lengths_throws() {
        val result = runCommand(
            command,
            DataObject.create {
                put("event_name", "purchase")
                put(
                    "parameters",
                    DataObject.create {
                        put(
                            FirebaseAnalytics.Param.ITEMS,
                            DataObject.create {
                                put(
                                    FirebaseAnalytics.Param.ITEM_ID,
                                    DataList.fromStringCollection(listOf("SKU1", "SKU2", "SKU3")),
                                )
                                put(
                                    FirebaseAnalytics.Param.ITEM_NAME,
                                    DataList.fromStringCollection(listOf("Widget", "Gadget")),
                                )
                            },
                        )
                    },
                )
            },
        )
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        verify(exactly = 0) { firebase.logEvent(any(), any()) }
    }

    @Test
    fun missing_event_name_fails_with_missing_parameter() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("missing"))
        verify(exactly = 0) { firebase.logEvent(any(), any()) }
    }

    @Test
    fun non_string_event_name_fails_with_invalid_parameter_type() {
        val result = runCommand(
            command,
            DataObject.create { put("event_name", DataObject.create { put("nested", "value") }) },
        )
        assertFalse(result.isSuccess)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CommandException)
        assertTrue(exception!!.message!!.contains("expected type"))
        verify(exactly = 0) { firebase.logEvent(any(), any()) }
    }

    @Test
    fun numeric_event_name_coerced_to_string() {
        val result = runCommand(command, DataObject.create { put("event_name", 42) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.logEvent("42", any()) }
    }

    @Test
    fun double_event_name_coerced_to_string() {
        val result = runCommand(command, DataObject.create { put("event_name", 3.14) })
        assertTrue(result.isSuccess)
        verify(exactly = 1) { firebase.logEvent("3.14", any()) }
    }
}
