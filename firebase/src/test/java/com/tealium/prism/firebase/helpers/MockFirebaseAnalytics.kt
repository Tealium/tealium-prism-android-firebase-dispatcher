package com.tealium.prism.firebase.helpers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface

/** In-memory [FirebaseAnalyticsInterface] that records every interaction for assertions. */
internal class MockFirebaseAnalytics : FirebaseAnalyticsInterface {

    data class LoggedEvent(val name: String, val parameters: Bundle?)
    data class UserProperty(val name: String, val value: String?)

    val loggedEvents = mutableListOf<LoggedEvent>()
    val userProperties = mutableListOf<UserProperty>()

    var lastUserId: String? = null
    var setUserIdCount = 0

    var resetAnalyticsDataCount = 0

    var lastDefaultParameters: Bundle? = null
    var setDefaultEventParametersCount = 0

    var lastConsentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>? = null
    var setConsentCount = 0

    var lastSessionTimeoutMillis: Long? = null
    var setSessionTimeoutCount = 0

    var lastAnalyticsEnabled: Boolean? = null
    var setAnalyticsEnabledCount = 0

    override fun logEvent(name: String, parameters: Bundle?) {
        loggedEvents.add(LoggedEvent(name, parameters))
    }

    override fun setUserId(userId: String?) {
        lastUserId = userId
        setUserIdCount++
    }

    override fun setUserProperty(name: String, value: String?) {
        userProperties.add(UserProperty(name, value))
    }

    override fun resetAnalyticsData() {
        resetAnalyticsDataCount++
    }

    override fun setDefaultEventParameters(parameters: Bundle?) {
        lastDefaultParameters = parameters
        setDefaultEventParametersCount++
    }

    override fun setConsent(
        consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>,
    ) {
        lastConsentSettings = consentSettings
        setConsentCount++
    }

    override fun setSessionTimeoutDuration(milliseconds: Long) {
        lastSessionTimeoutMillis = milliseconds
        setSessionTimeoutCount++
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        lastAnalyticsEnabled = enabled
        setAnalyticsEnabledCount++
    }
}
