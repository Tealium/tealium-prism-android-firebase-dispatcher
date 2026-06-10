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
import com.tealium.prism.firebase.internal.FirebaseConstants

/**
 * Updates the Firebase Analytics session timeout at runtime.
 *
 * A session is a period of time during which a user is actively engaged with the app.
 * Payload value is expressed in seconds — matching the cross-platform schema — and
 * converted to milliseconds before being forwarded to the Android SDK.
 *
 * Expected payload:
 * ```json
 * {
 *   "command_name": "setsessiontimeout",
 *   "session_timeout_seconds": 3600
 * }
 * ```
 */
internal class SetSessionTimeoutCommand(
    private val firebaseInstance: FirebaseAnalyticsInterface,
) : Command {

    override val name: String = FirebaseCommand.SET_SESSION_TIMEOUT.commandName

    override fun execute(
        payload: DataObject,
        callback: Callback<TealiumResult<Unit>>,
    ): Disposable {
        try {
            val path = FirebaseDestination.SessionTimeout.asJsonObjectPath()
            val seconds = payload.extract(path, LenientConverters.DOUBLE)
                ?: throw CommandException.invalidParameterType(
                    path.toString(),
                    "numeric value (seconds)",
                )
            val millis = (seconds * FirebaseConstants.MILLISECONDS_PER_SECOND).toLong()
            firebaseInstance.setSessionTimeoutDuration(millis)
            callback.success(Unit)
        } catch (t: Throwable) {
            callback.failure(t)
        }
        return Disposables.disposed()
    }

}
