package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.data.LenientConverters
import com.tealium.prism.core.api.misc.Callback
import com.tealium.prism.core.api.misc.TealiumResult
import com.tealium.prism.core.api.misc.failure
import com.tealium.prism.core.api.misc.success
import com.tealium.prism.core.api.pubsub.Disposable
import com.tealium.prism.core.api.pubsub.Disposables
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
internal class LogEventCommand(
    private val firebaseInstance: FirebaseAnalyticsInterface,
) : Command {

    override val name: String = FirebaseCommand.LOG_EVENT.commandName

    override fun execute(
        payload: DataObject,
        callback: Callback<TealiumResult<Unit>>,
    ): Disposable {
        try {
            val eventNamePath = FirebaseDestination.EventName.asJsonObjectPath()
            val eventName = payload.extract(eventNamePath, LenientConverters.STRING)
                ?: throw CommandException.missingParameter(eventNamePath.toString())
            val paramsPath = FirebaseDestination.EventParams.asJsonObjectPath()
            val paramsDict = payload.extractDataObject(paramsPath)
            val bundle = ParametersBundleBuilder.build(paramsDict)
            firebaseInstance.logEvent(eventName, bundle)
            callback.success(Unit)
        } catch (t: Throwable) {
            callback.failure(t)
        }
        return Disposables.disposed()
    }
}
