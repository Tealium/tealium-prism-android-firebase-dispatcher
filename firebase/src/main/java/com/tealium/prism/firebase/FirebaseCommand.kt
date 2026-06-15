package com.tealium.prism.firebase

import com.tealium.prism.core.api.command.CommandName

/**
 * Type-safe Firebase Analytics commands for use with [FirebaseMappings.mapCommand].
 *
 * [commandName] values match the payload strings used by the dispatcher
 * (all lowercase, no separators).
 */
enum class FirebaseCommand : CommandName {
    LOG_EVENT,
    SET_USER_ID,
    SET_USER_PROPERTY,
    RESET_DATA,
    SET_DEFAULT_PARAMETERS,
    SET_CONSENT,
    SET_SESSION_TIMEOUT,
    SET_ANALYTICS_COLLECTION_ENABLED;

    override val commandName: String =
        name.lowercase().replace("_", "")
}
