package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.command.CommandName
import com.tealium.prism.core.api.data.DataItem
import com.tealium.prism.core.api.data.DataItemConverter
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.data.JsonObjectPathConvertible
import com.tealium.prism.core.api.data.LenientConverters

// TODO - move all these into `prism-core` for future Commands to use, and remove `internal` modifier

/**
 * Convenience method to create a Synchronous [Command] using its [CommandName]
 *
 * @param commandName The name of the command to create
 * @param block The handler for the command
 */
internal fun synchronous(commandName: CommandName, block: (DataObject) -> Unit) =
    Command.synchronous(commandName.commandName, block)

// Section: extract JsonObjectPathConvertible

/**
 * Convenience method to extract a [T] from this [DataObject] at the given [path] using the given [converter]
 *
 * @param path The path to extract the [DataObject] from
 * @param converter The converter required to create [T] from [DataItem]
 */
internal fun <T> DataObject.extract(path: JsonObjectPathConvertible, converter: DataItemConverter<T>): T? =
    extract(path.asJsonObjectPath(), converter)

/**
 * Convenience method to extract a [DataObject] from this [DataObject] at the given [path]
 *
 * @param path The path to extract the [DataObject] from
 */
internal fun DataObject.extractDataObject(path: JsonObjectPathConvertible) =
    extract(path.asJsonObjectPath(), DataItem::getDataObject)


// Section - require

/**
 * Extracts a value from this [DataObject] at the given [path] using the [converter] to convert it
 * to [T]
 *
 * If no value exists at the given [path], then [CommandException.missingParameter] is thrown.
 *
 * If the value does exist at the given [path] but cannot be converted to [T] then
 * [CommandException.invalidParameterType] is thrown.
 *
 * @return The retrieved or converted value
 */
internal fun <T> DataObject.require(path: JsonObjectPathConvertible, converter: DataItemConverter<T>, expectedType: String): T {
    val jsonPath = path.asJsonObjectPath()

    val extractedItem = extract(jsonPath)
        ?: throw CommandException.missingParameter(jsonPath.toString())

    return converter.convert(extractedItem)
        ?: throw CommandException.invalidParameterType(jsonPath.toString(), expectedType)
}

/**
 * Extracts a [String] value from this [DataObject] at the given [path] using the [converter].
 * The [LenientConverters] are used by default.
 *
 * If no value exists at the given [path], then [CommandException.missingParameter] is thrown.
 *
 * If the value does exist at the given [path] but cannot be converted to a [String] then
 * [CommandException.invalidParameterType] is thrown.
 *
 * @return The retrieved or converted [String] value
 */
@Throws(CommandException::class)
@JvmOverloads
internal fun DataObject.requireString(path: JsonObjectPathConvertible, converter: DataItemConverter<String> = LenientConverters.STRING): String =
    require(path, converter, "String")

/**
 * Extracts a [Boolean] value from this [DataObject] at the given [path] using the [converter].
 * The [LenientConverters] are used by default.
 *
 * If no value exists at the given [path], then [CommandException.missingParameter] is thrown.
 *
 * If the value does exist at the given [path] but cannot be converted to a [Boolean] then
 * [CommandException.invalidParameterType] is thrown.
 *
 * @return The retrieved or converted [Boolean] value
 */
@Throws(CommandException::class)
@JvmOverloads
internal fun DataObject.requireBoolean(path: JsonObjectPathConvertible, converter: DataItemConverter<Boolean> = LenientConverters.BOOLEAN): Boolean =
    require(path, converter, "boolean (true/false)")

/**
 * Extracts a [Double] value from this [DataObject] at the given [path] using the [converter].
 * The [LenientConverters] are used by default.
 *
 * If no value exists at the given [path], then [CommandException.missingParameter] is thrown.
 *
 * If the value does exist at the given [path] but cannot be converted to a [Double] then
 * [CommandException.invalidParameterType] is thrown.
 *
 * @return The retrieved or converted [Double] value
 */
@Throws(CommandException::class)
@JvmOverloads
internal fun DataObject.requireDouble(path: JsonObjectPathConvertible, converter: DataItemConverter<Double> = LenientConverters.DOUBLE): Double =
    require(path, converter, "numeric value")


