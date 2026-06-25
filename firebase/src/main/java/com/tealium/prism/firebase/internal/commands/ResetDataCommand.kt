package com.tealium.prism.firebase.internal.commands

import com.tealium.prism.core.api.command.Command
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
internal fun resetDataCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    synchronous(FirebaseCommand.RESET_DATA) {
        firebaseInstance.resetAnalyticsData()
    }
