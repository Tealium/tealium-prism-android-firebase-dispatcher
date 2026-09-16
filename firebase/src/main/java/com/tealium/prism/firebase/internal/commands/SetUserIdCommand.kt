package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
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
internal fun setUserIdCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    synchronous(FirebaseCommand.SET_USER_ID) { payload ->
        val userId = payload.requireString(FirebaseDestination.UserId)
        firebaseInstance.setUserId(userId.ifEmpty { null })
    }
