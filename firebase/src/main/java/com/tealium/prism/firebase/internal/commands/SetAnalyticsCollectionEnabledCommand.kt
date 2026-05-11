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
internal class SetAnalyticsCollectionEnabledCommand(
    private val firebaseInstance: FirebaseAnalyticsInterface,
) : Command {

    override val name: String = FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED.commandName

    override fun execute(
        payload: DataObject,
        callback: Callback<TealiumResult<Unit>>,
    ): Disposable {
        try {
            val path = FirebaseDestination.AnalyticsEnabled.asJsonObjectPath()
            val enabled = payload.extract(path, LenientConverters.BOOLEAN)
                ?: throw CommandException.invalidParameterType(
                    path.toString(),
                    "boolean (true/false)",
                )
            firebaseInstance.setAnalyticsCollectionEnabled(enabled)
            callback.success(Unit)
        } catch (t: Throwable) {
            callback.failure(t)
        }
        return Disposables.disposed()
    }
}
