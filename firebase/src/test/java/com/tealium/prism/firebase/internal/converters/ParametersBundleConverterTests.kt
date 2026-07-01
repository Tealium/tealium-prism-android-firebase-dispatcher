package com.tealium.prism.firebase.internal.converters

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParametersBundleConverterTests {

    @Test
    fun null_input_returns_null() {
        assertNull(ParametersBundleConverter.build(null))
    }

    @Test
    fun empty_object_returns_null() {
        assertNull(ParametersBundleConverter.build(DataObject.create {}))
    }

    @Test
    fun scalar_values_map_to_matching_bundle_types() {
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put("string_key", "value")
                put("bool_key", true)
                put("long_key", 42L)
                put("double_key", 9.99)
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        assertEquals("value", bundle.getString("string_key"))
        assertEquals(true, bundle.getBoolean("bool_key"))
        assertEquals(42L, bundle.getLong("long_key"))
        assertEquals(9.99, bundle.getDouble("double_key"), 0.0)
    }

    @Test
    fun parallel_arrays_transpose_into_item_bundles() {
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put(
                    FirebaseAnalytics.Param.ITEMS,
                    DataObject.create {
                        put("item_id", DataList.fromStringCollection(listOf("SKU1", "SKU2")))
                        put("price", DataList.fromDoubleCollection(listOf(29.99, 70.00)))
                    }
                )
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        val items = bundle.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)
        assertNotNull(items)
        requireNotNull(items)
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString("item_id"))
        assertEquals(29.99, items[0].getDouble("price"), 0.0)
        assertEquals("SKU2", items[1].getString("item_id"))
        assertEquals(70.00, items[1].getDouble("price"), 0.0)
    }

    @Test
    fun scalar_item_values_wrap_as_single_element_arrays() {
        // A single-item payload expressed as scalars (no arrays) produces one item bundle.
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put(
                    FirebaseAnalytics.Param.ITEMS,
                    DataObject.create {
                        put("item_id", "SKU1")
                        put("price", 9.99)
                    }
                )
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        val items = bundle.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)
        assertNotNull(items)
        requireNotNull(items)
        assertEquals(1, items.size)
        assertEquals("SKU1", items[0].getString("item_id"))
        assertEquals(9.99, items[0].getDouble("price"), 0.0)
    }

    @Test
    fun array_of_objects_keeps_empty_slot_for_non_dict_entries() {
        // Non-dict entries are kept as empty bundles to preserve item index alignment.
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put(
                    FirebaseAnalytics.Param.ITEMS,
                    DataList.create {
                        add(DataObject.create {
                            put("item_id", "SKU1")
                            put("price", 29.99)
                        })
                        add("not_a_dict")
                    }
                )
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        val items = bundle.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)
        assertNotNull(items)
        requireNotNull(items)
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString("item_id"))
        assertEquals(true, items[1].isEmpty)
    }

    @Test
    fun array_of_objects_passes_through_unchanged() {
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put(
                    FirebaseAnalytics.Param.ITEMS,
                    DataList.create {
                        add(DataObject.create {
                            put("item_id", "SKU1")
                            put("price", 29.99)
                        })
                        add(DataObject.create {
                            put("item_id", "SKU2")
                            put("price", 70.00)
                        })
                    }
                )
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        val items = bundle.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)
        assertNotNull(items)
        requireNotNull(items)
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString("item_id"))
        assertEquals("SKU2", items[1].getString("item_id"))
    }

    @Test
    fun null_value_is_silently_skipped() {
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put("keep", "value")
                putNull("drop")
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        assertEquals("value", bundle.getString("keep"))
        assertEquals(false, bundle.containsKey("drop"))
    }

    @Test
    fun only_null_values_yield_null_bundle() {
        val bundle = ParametersBundleConverter.build(
            DataObject.create { putNull("drop") }
        )
        assertNull(bundle)
    }

    @Test
    fun empty_items_list_produces_empty_items_array() {
        val bundle = ParametersBundleConverter.build(
            DataObject.create {
                put(FirebaseAnalytics.Param.ITEMS, DataList.create {})
            }
        )
        assertNotNull(bundle)
        requireNotNull(bundle)
        val items = bundle.getParcelableArrayList(FirebaseAnalytics.Param.ITEMS, Bundle::class.java)
        assertNotNull(items)
        assertEquals(0, items?.size)
    }

    @Test
    fun mismatched_parallel_arrays_throw_arrayLengthMismatch() {
        assertThrows(CommandException::class.java) {
            ParametersBundleConverter.build(
                DataObject.create {
                    put(
                        FirebaseAnalytics.Param.ITEMS,
                        DataObject.create {
                            put("item_id", DataList.fromStringCollection(listOf("SKU1", "SKU2")))
                            put("price", DataList.fromDoubleCollection(listOf(29.99)))
                        }
                    )
                }
            )
        }
    }
}
