package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.data.DataObject
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
 * Sets (or clears) Firebase Analytics default event parameters.
 *
 * Default parameters are automatically included with every event logged to Firebase.
 * They persist across app runs and are of lower precedence than event-level parameters.
 * Missing or empty [FirebaseDestination.DefaultParams] clears all default parameters.
 *
 * Expected payload:
 * ```json
 * {
 *   "command_name": "setdefaultparameters",
 *   "parameters": {
 *     "version": "2.1.0",
 *     "language": "en",
 *     "country": "US"
 *   }
 * }
 * ```
 */
internal class SetDefaultParametersCommand(
    private val firebaseInstance: FirebaseAnalyticsInterface,
) : Command {

    override val name: String = FirebaseCommand.SET_DEFAULT_PARAMETERS.commandName

    override fun execute(
        payload: DataObject,
        callback: Callback<TealiumResult<Unit>>,
    ): Disposable {
        try {
            val path = FirebaseDestination.DefaultParams.asJsonObjectPath()
            val params = payload.extractDataObject(path)
            val bundle = ParametersBundleBuilder.build(params)
            firebaseInstance.setDefaultEventParameters(bundle)
            callback.success(Unit)
        } catch (t: Throwable) {
            callback.failure(t)
        }
        return Disposables.disposed()
    }
}
