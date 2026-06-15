package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.core.api.data.LenientConverters
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface

/**
 * Toggles Firebase Analytics data collection at runtime.
 *
 * When disabled, Firebase Analytics stops collecting data but retains previously collected data.
 *
 * Expected payload:
 * ```json
 * {
 *   "command_name": "setanalyticscollectionenabled",
 *   "analytics_collection_enabled": true
 * }
 * ```
 */
internal fun setAnalyticsCollectionEnabledCommand(
    firebaseInstance: FirebaseAnalyticsInterface,
): Command =
    Command.synchronous(FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED.commandName) { payload ->
        val path = FirebaseDestination.AnalyticsEnabled.asJsonObjectPath()
        val enabledItem = payload.extract(path)
            ?: throw CommandException.missingParameter(path.toString())
        val enabled = LenientConverters.BOOLEAN.convert(enabledItem)
            ?: throw CommandException.invalidParameterType(
                path.toString(),
                "boolean (true/false)",
            )
        firebaseInstance.setAnalyticsCollectionEnabled(enabled)
    }
