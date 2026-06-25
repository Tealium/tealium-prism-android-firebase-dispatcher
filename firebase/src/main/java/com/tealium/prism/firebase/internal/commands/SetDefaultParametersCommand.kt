package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
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
internal fun setDefaultParametersCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    synchronous(FirebaseCommand.SET_DEFAULT_PARAMETERS) { payload ->
        val bundle = payload.extract(FirebaseDestination.DefaultParams, ParametersBundleBuilder)
        firebaseInstance.setDefaultEventParameters(bundle)
    }
