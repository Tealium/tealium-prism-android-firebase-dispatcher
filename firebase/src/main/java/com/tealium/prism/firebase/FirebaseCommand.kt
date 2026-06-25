package com.tealium.prism.firebase

import com.tealium.prism.core.api.command.CommandName

/**
 * Type-safe Firebase Analytics commands for use with [FirebaseMappings.mapCommand].
 *
 * [commandName] values match the payload strings used by the dispatcher
 * (all lowercase, no separators).
 */
enum class FirebaseCommand(
    override val commandName: String
) : CommandName {
    LOG_EVENT("logevent"),
    SET_USER_ID("setuserid"),
    SET_USER_PROPERTY("setuserproperty"),
    RESET_DATA("resetdata"),
    SET_DEFAULT_PARAMETERS("setdefaultparameters"),
    SET_CONSENT("setconsent"),
    SET_SESSION_TIMEOUT("setsessiontimeout"),
    SET_ANALYTICS_COLLECTION_ENABLED("setanalyticscollectionenabled");
}
