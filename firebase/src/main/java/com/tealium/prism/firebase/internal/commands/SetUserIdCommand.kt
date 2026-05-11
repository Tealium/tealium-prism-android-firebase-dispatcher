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
 * Sets the Firebase Analytics user id.
 *
 * Associates analytics data with a specific user. Must be used in accordance with
 * Google's Privacy Policy. Empty string clears the user id.
 *
 * Expected payload:
 * ```json
 * {
 *   "command_name": "setuserid",
 *   "user_id": "USER_12345"
 * }
 * ```
 */
internal class SetUserIdCommand(
    private val firebaseInstance: FirebaseAnalyticsInterface,
) : Command {

    override val name: String = FirebaseCommand.SET_USER_ID.commandName

    override fun execute(
        payload: DataObject,
        callback: Callback<TealiumResult<Unit>>,
    ): Disposable {
        try {
            val path = FirebaseDestination.UserId.asJsonObjectPath()
            val userId = payload.extract(path, LenientConverters.STRING)
                ?: throw CommandException.missingParameter(path.toString())
            firebaseInstance.setUserId(userId.ifEmpty { null })
            callback.success(Unit)
        } catch (t: Throwable) {
            callback.failure(t)
        }
        return Disposables.disposed()
    }
}
