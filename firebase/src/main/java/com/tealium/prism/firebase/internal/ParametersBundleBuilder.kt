package com.tealium.prism.firebase.internal

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataItem
import com.tealium.prism.core.api.data.DataItemConverter
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject

/**
 * Converts Tealium [DataObject] payloads into Firebase-compatible [Bundle]s.
 *
 * Scalar values (String, Long, Int, Double, Boolean) map directly to the matching
 * `Bundle.putX` call. The reserved `items` key is handled specially — it accepts two
 * input shapes and is always forwarded as an `ArrayList<Bundle>` under
 * `FirebaseAnalytics.Param.ITEMS`:
 *
 * 1. Array of objects (Firebase-ready): each list entry is already a dictionary, copied
 *    straight into a child `Bundle`.
 * 2. Parallel arrays (Tealium convention): each key maps to an array of equal length;
 *    the builder transposes them into a list of per-item bundles.
 *
 * Any array-length mismatch in shape (2) throws [CommandException.arrayLengthMismatch].
 *
 * Implements [DataItemConverter] so callers can extract parameters type-safely via
 * `payload.extract(path, ParametersBundleBuilder)`.
 */
internal object ParametersBundleBuilder : DataItemConverter<Bundle> {

    private const val ITEMS_KEY = FirebaseAnalytics.Param.ITEMS

    /**
     * Converts the [dataItem] to a parameters [Bundle], or `null` when it is not a
     * [DataObject] or the resulting bundle would be empty.
     */
    override fun convert(dataItem: DataItem): Bundle? = build(dataItem.getDataObject())

    /**
     * Builds a [Bundle] from the supplied [params] dictionary. Returns `null` when the
     * resulting bundle would be empty so that callers can preserve "no parameters" semantics.
     */
    fun build(params: DataObject?): Bundle? {
        if (params == null) return null
        val bundle = Bundle()
        for ((key, dataItem) in params) {
            if (key == ITEMS_KEY) {
                val items = buildItems(dataItem) ?: continue
                bundle.putParcelableArrayList(ITEMS_KEY, ArrayList(items))
            } else {
                putScalar(bundle, key, dataItem)
            }
        }
        return if (bundle.isEmpty) null else bundle
    }

    private fun buildItems(dataItem: DataItem): List<Bundle>? {
        dataItem.getDataList()?.let { return buildFromArrayOfObjects(it) }
        dataItem.getDataObject()?.let { return buildFromParallelArrays(it) }
        return null
    }

    private fun buildFromArrayOfObjects(list: DataList): List<Bundle> {
        val items = ArrayList<Bundle>(list.size)
        for (entry in list) {
            val dict = entry.getDataObject() ?: continue
            val itemBundle = Bundle()
            for ((k, v) in dict) {
                putScalar(itemBundle, k, v)
            }
            if (!itemBundle.isEmpty) items.add(itemBundle)
        }
        return items
    }

    private fun buildFromParallelArrays(dict: DataObject): List<Bundle> {
        val arrays = LinkedHashMap<String, DataList>()
        for ((k, v) in dict) {
            v.getDataList()?.let { arrays[k] = it }
        }
        if (arrays.isEmpty()) return emptyList()

        val referenceKey = arrays.keys.first()
        val referenceSize = arrays.getValue(referenceKey).size

        for ((key, array) in arrays) {
            if (array.size != referenceSize) {
                throw CommandException.arrayLengthMismatch(referenceKey, key)
            }
        }
        if (referenceSize == 0) return emptyList()

        val items = ArrayList<Bundle>(referenceSize)
        for (index in 0 until referenceSize) {
            val itemBundle = Bundle()
            for ((key, array) in arrays) {
                val item = array.get(index) ?: continue
                putScalar(itemBundle, key, item)
            }
            if (!itemBundle.isEmpty) items.add(itemBundle)
        }
        return items
    }

    private fun putScalar(bundle: Bundle, key: String, item: DataItem) {
        when {
            item.isString() -> bundle.putString(key, item.getString())
            item.isBoolean() -> bundle.putBoolean(key, item.getBoolean()!!)
            item.isLong() -> bundle.putLong(key, item.getLong()!!)
            item.isInt() -> bundle.putInt(key, item.getInt()!!)
            item.isDouble() -> bundle.putDouble(key, item.getDouble()!!)
            item.isNumber() -> item.getDouble()?.let { bundle.putDouble(key, it) }
        }
    }
}
