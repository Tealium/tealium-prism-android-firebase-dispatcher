package com.tealium.prism.firebase.internal.commands

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.firebase.helpers.MockFirebaseAnalytics
import com.tealium.prism.firebase.helpers.runCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogEventCommandTests {

    private val firebase = MockFirebaseAnalytics()
    private val command = LogEventCommand(firebase)

    @Test
    fun forwards_event_without_parameters() {
        val result = runCommand(command, DataObject.create { put("event_name", "login") })
        assertTrue(result.isSuccess)
        assertEquals(1, firebase.loggedEvents.size)
        assertEquals("login", firebase.loggedEvents[0].name)
        assertNull(firebase.loggedEvents[0].parameters)
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
        val logged = firebase.loggedEvents.single()
        assertEquals("purchase", logged.name)
        assertNotNull(logged.parameters)
        assertEquals(99.99, logged.parameters!!.getDouble("value"), 0.0)
        assertEquals("USD", logged.parameters!!.getString("currency"))
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
        val logged = firebase.loggedEvents.single()
        assertEquals(true, logged.parameters!!.getBoolean("is_premium"))
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
        val params = firebase.loggedEvents.single().parameters!!
        assertEquals(99.99, params.getDouble("value"), 0.0)
        assertEquals("USD", params.getString("currency"))
        val items = params.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)!!
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
        val items = firebase.loggedEvents
            .single()
            .parameters!!
            .getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)!!
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
        val items = firebase.loggedEvents
            .single()
            .parameters!!
            .getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)!!
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
        assertTrue(firebase.loggedEvents.isEmpty())
    }

    @Test
    fun missing_event_name_fails() {
        val result = runCommand(command, DataObject.create {})
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is CommandException)
        assertTrue(firebase.loggedEvents.isEmpty())
    }

    @Test
    fun numeric_event_name_coerced_to_string() {
        val result = runCommand(command, DataObject.create { put("event_name", 42) })
        assertTrue(result.isSuccess)
        assertEquals("42", firebase.loggedEvents.single().name)
    }

    @Test
    fun double_event_name_coerced_to_string() {
        val result = runCommand(command, DataObject.create { put("event_name", 3.14) })
        assertTrue(result.isSuccess)
        assertEquals("3.14", firebase.loggedEvents.single().name)
    }
}
