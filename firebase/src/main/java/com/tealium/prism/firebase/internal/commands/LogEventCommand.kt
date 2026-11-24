package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.data.LenientConverters
import com.tealium.prism.core.api.data.extract
import com.tealium.prism.core.api.data.requireNonBlank
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface
import com.tealium.prism.firebase.internal.converters.ParametersBundleConverter

/**
 * Logs events to Firebase Analytics with optional parameters and items.
 *
 * Supports two item payload formats:
 *
 * Format 1 — Parallel Arrays (Tealium convention):
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
 *
 * A missing or blank `event_name` fails the command. Firebase Analytics requires event names of
 * 1 to 40 characters that start with a letter.
 */
internal fun logEventCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    Command.synchronous(FirebaseCommand.LOG_EVENT) { payload ->
        val eventName = payload.requireNonBlank(FirebaseDestination.EventName, LenientConverters.STRING)
        val bundle = payload.extract(FirebaseDestination.EventParams, ParametersBundleConverter)

        firebaseInstance.logEvent(eventName, bundle)
    }
