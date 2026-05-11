package com.tealium.prism.firebase.internal

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Internal abstraction over the Firebase Analytics SDK used by all Firebase commands.
 *
 * Enables unit tests to substitute a fake implementation without touching the real SDK.
 */
internal interface FirebaseAnalyticsInterface {

    fun logEvent(name: String, parameters: Bundle?)

    fun setUserId(userId: String?)

    fun setUserProperty(name: String, value: String?)

    fun resetAnalyticsData()

    fun setDefaultEventParameters(parameters: Bundle?)

    fun setConsent(
        consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>,
    )

    fun setSessionTimeoutDuration(milliseconds: Long)

    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
