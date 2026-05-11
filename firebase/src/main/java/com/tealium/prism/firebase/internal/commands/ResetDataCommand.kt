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
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface

/**
 * Clears all Firebase Analytics data and resets the app instance id.
 *
 * Clears all analytics data for this app instance from the device and resets the app instance ID.
 * Typically used when a user logs out or when privacy regulations require data deletion.
 *
 * Expected payload:
 * ```json
 * { "command_name": "resetdata" }
 * ```
 */
internal class ResetDataCommand(
    private val firebaseInstance: FirebaseAnalyticsInterface,
) : Command {

    override val name: String = FirebaseCommand.RESET_DATA.commandName

    override fun execute(
        payload: DataObject,
        callback: Callback<TealiumResult<Unit>>,
    ): Disposable {
        try {
            firebaseInstance.resetAnalyticsData()
            callback.success(Unit)
        } catch (t: Throwable) {
            callback.failure(t)
        }
        return Disposables.disposed()
    }
}
