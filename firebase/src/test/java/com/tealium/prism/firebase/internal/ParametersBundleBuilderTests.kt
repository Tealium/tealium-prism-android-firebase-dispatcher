package com.tealium.prism.firebase.internal

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
class ParametersBundleBuilderTests {

    @Test
    fun null_input_returns_null() {
        assertNull(ParametersBundleBuilder.build(null))
    }

    @Test
    fun empty_object_returns_null() {
        assertNull(ParametersBundleBuilder.build(DataObject.create {}))
    }

    @Test
    fun scalar_values_map_to_matching_bundle_types() {
        val bundle = ParametersBundleBuilder.build(
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
        val bundle = ParametersBundleBuilder.build(
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
        val items = bundle.getParcelableArrayList<Bundle>(FirebaseAnalytics.Param.ITEMS)
        assertNotNull(items)
        requireNotNull(items)
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString("item_id"))
        assertEquals(29.99, items[0].getDouble("price"), 0.0)
        assertEquals("SKU2", items[1].getString("item_id"))
        assertEquals(70.00, items[1].getDouble("price"), 0.0)
    }

    @Test
    fun array_of_objects_passes_through_unchanged() {
        val bundle = ParametersBundleBuilder.build(
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
        val items = bundle.getParcelableArrayList<Bundle>(FirebaseAnalytics.Param.ITEMS)
        assertNotNull(items)
        requireNotNull(items)
        assertEquals(2, items.size)
        assertEquals("SKU1", items[0].getString("item_id"))
        assertEquals("SKU2", items[1].getString("item_id"))
    }

    @Test
    fun null_value_is_silently_skipped() {
        val bundle = ParametersBundleBuilder.build(
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
        val bundle = ParametersBundleBuilder.build(
            DataObject.create { putNull("drop") }
        )
        assertNull(bundle)
    }

    @Test
    fun mismatched_parallel_arrays_throw_arrayLengthMismatch() {
        assertThrows(CommandException::class.java) {
            ParametersBundleBuilder.build(
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
