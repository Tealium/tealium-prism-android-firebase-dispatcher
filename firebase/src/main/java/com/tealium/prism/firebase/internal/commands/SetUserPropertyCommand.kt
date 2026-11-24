package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataItem
import com.tealium.prism.core.api.data.ExtractionException
import com.tealium.prism.core.api.data.LenientConverters
import com.tealium.prism.core.api.data.requireDataItem
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface

/**
 * Sets one or many Firebase Analytics user properties.
 *
 * Handles both scalar and parallel-array payload shapes. Values persist across sessions.
 * Empty string clears the property. Names that cannot be read as a string are skipped; the
 * command fails when no name is left, or when the two arrays have different lengths.
 *
 * Single property:
 * ```json
 * {
 *   "command_name": "setuserproperty",
 *   "property_name": "tier",
 *   "property_value": "premium"
 * }
 * ```
 *
 * Multiple properties (arrays):
 * ```json
 * {
 *   "command_name": "setuserproperty",
 *   "property_name": ["subscription_tier", "user_level"],
 *   "property_value": ["premium", "expert"]
 * }
 * ```
 */
internal fun setUserPropertyCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    Command.synchronous(FirebaseCommand.SET_USER_PROPERTY) { payload ->
        val namesPath = FirebaseDestination.UserPropertyName.asJsonObjectPath()
        val valuesPath = FirebaseDestination.UserPropertyValue.asJsonObjectPath()
        val namesItem = payload.requireDataItem(namesPath)
        val valuesItem = payload.requireDataItem(valuesPath)

        val names = extractStringList(namesItem)
        val values = extractStringList(valuesItem)

        if (names.all { it == null }) {
            throw ExtractionException.Empty(namesPath.toString())
        }
        if (names.size != values.size) {
            throw CommandException.arrayLengthMismatch(
                namesPath.toString(),
                valuesPath.toString(),
            )
        }

        for ((index, rawName) in names.withIndex()) {
            val propertyName = rawName ?: continue
            val rawValue = values[index]
            val propertyValue = if (rawValue.isNullOrEmpty()) null else rawValue
            firebaseInstance.setUserProperty(propertyName, propertyValue)
        }
    }

/**
 * Extracts a payload item as a list of (nullable) strings, accepting either a single
 * scalar value or an array. Used by [setUserPropertyCommand] to handle both the scalar
 * and parallel-array shapes of the user property payload.
 */
private fun extractStringList(item: DataItem): List<String?> {
    item.getDataList()?.let { list ->
        return (0 until list.size).map { list.getString(it) }
    }
    return listOf(LenientConverters.STRING.convert(item))
}
