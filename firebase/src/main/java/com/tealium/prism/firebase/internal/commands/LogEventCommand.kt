package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.LenientConverters
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import com.tealium.prism.firebase.internal.ParametersBundleBuilder

/**
 * Logs events to Firebase Analytics with optional parameters and items.
 *
 * Supports two item payload formats:
 *
 * Format 1 — Object of Arrays (Tealium convention):
 * ```json
 * {
 *   "command_name": "logevent",
 *   "event_name": "purchase",
 *   "parameters": {
 *     "value": 99.99,
 *     "currency": "USD",
 *     "items": {
 *       "item_id": ["SKU001", "SKU002"],
 *       "item_name": ["Widget", "Gadget"],
 *       "price": [29.99, 70.00]
 *     }
 *   }
 * }
 * ```
 *
 * Format 2 — Array of Objects (Firebase-ready):
 * ```json
 * {
 *   "command_name": "logevent",
 *   "event_name": "purchase",
 *   "parameters": {
 *     "value": 99.99,
 *     "currency": "USD",
 *     "items": [
 *       {"item_id": "SKU001", "item_name": "Widget", "price": 29.99},
 *       {"item_id": "SKU002", "item_name": "Gadget", "price": 70.00}
 *     ]
 *   }
 * }
 * ```
 */
internal fun logEventCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    Command.synchronous(FirebaseCommand.LOG_EVENT.commandName) { payload ->
        val eventNamePath = FirebaseDestination.EventName.asJsonObjectPath()
        val eventNameItem = payload.extract(eventNamePath)
            ?: throw CommandException.missingParameter(eventNamePath.toString())
        val eventName = LenientConverters.STRING.convert(eventNameItem)
            ?: throw CommandException.invalidParameterType(eventNamePath.toString(), "string")
        val paramsPath = FirebaseDestination.EventParams.asJsonObjectPath()
        val bundle = payload.extract(paramsPath, ParametersBundleBuilder)
        firebaseInstance.logEvent(eventName, bundle)
    }
