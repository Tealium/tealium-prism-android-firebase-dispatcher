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
    /** Logs an analytics event via `FirebaseAnalytics.logEvent`. */
    LOG_EVENT("logevent"),

    /** Sets the user ID via `FirebaseAnalytics.setUserId`. */
    SET_USER_ID("setuserid"),

    /** Sets a user property via `FirebaseAnalytics.setUserProperty`. */
    SET_USER_PROPERTY("setuserproperty"),

    /** Clears all analytics data for this app instance via `FirebaseAnalytics.resetAnalyticsData`. */
    RESET_DATA("resetdata"),

    /** Sets parameters added to every subsequent event via `FirebaseAnalytics.setDefaultEventParameters`. */
    SET_DEFAULT_PARAMETERS("setdefaultparameters"),

    /** Sets the Firebase consent settings via `FirebaseAnalytics.setConsent`. */
    SET_CONSENT("setconsent"),

    /** Sets the session timeout via `FirebaseAnalytics.setSessionTimeoutDuration`. */
    SET_SESSION_TIMEOUT("setsessiontimeout"),

    /** Enables or disables analytics collection via `FirebaseAnalytics.setAnalyticsCollectionEnabled`. */
    SET_ANALYTICS_COLLECTION_ENABLED("setanalyticscollectionenabled");
}
