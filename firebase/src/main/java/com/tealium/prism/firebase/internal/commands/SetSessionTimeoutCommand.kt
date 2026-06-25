package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
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
internal fun setSessionTimeoutCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    synchronous(FirebaseCommand.SET_SESSION_TIMEOUT) { payload ->
        val seconds = payload.requireDouble(FirebaseDestination.SessionTimeout)
        val millis = (seconds * FirebaseConstants.MILLISECONDS_PER_SECOND).toLong()
        firebaseInstance.setSessionTimeoutDuration(millis)
    }
