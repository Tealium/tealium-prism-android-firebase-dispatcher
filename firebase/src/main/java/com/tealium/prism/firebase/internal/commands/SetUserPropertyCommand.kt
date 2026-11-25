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
 * Empty string clears the property. Names and values are read leniently, so numbers and booleans
 * become strings.
 *
 * The payload is checked in this order:
 * 1. `property_name` must be a value that can be read as a string, or an array; otherwise
 *    the command fails with a type mismatch.
 * 2. `property_name` and `property_value` must have the same length (a single value counts as one).
 * 3. At least one name must be usable. Array entries that cannot be read as a string are skipped.
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
        val names = payload.requireDataItem(namesPath).toStringList()
            ?: throw ExtractionException.TypeMismatch(namesPath.toString(), NAMES_EXPECTED_TYPE)
        // A value that can't be read as a string clears the property, like an explicit null.
        val values = payload.requireDataItem(valuesPath).toStringList() ?: listOf(null)

        if (names.size != values.size) {
            throw CommandException.arrayLengthMismatch(
                namesPath.toString(),
                valuesPath.toString(),
            )
        }
        if (names.all { it == null }) {
            throw ExtractionException.Empty(namesPath.toString())
        }

        for ((index, rawName) in names.withIndex()) {
            val propertyName = rawName ?: continue
            val rawValue = values[index]
            val propertyValue = if (rawValue.isNullOrEmpty()) null else rawValue
            firebaseInstance.setUserProperty(propertyName, propertyValue)
        }
    }

private const val NAMES_EXPECTED_TYPE = "String or array of String"

/**
 * Reads this payload item as a list of strings, accepting either a single value or an array.
 * Used by [setUserPropertyCommand] to handle both the scalar and parallel-array shapes of the
 * user property payload.
 *
 * Every value is read with [LenientConverters.STRING]. An array entry that can't be read as a
 * string, and an explicit `null`, become `null`.
 *
 * @return The strings; or `null` if this item is neither an array, `null`, nor a value that can be
 * read as a string.
 */
private fun DataItem.toStringList(): List<String?>? {
    getDataList()?.let { list ->
        return list.map(LenientConverters.STRING::convert)
    }
    if (isNull()) {
        return listOf(null)
    }
    return LenientConverters.STRING.convert(this)?.let(::listOf)
}
