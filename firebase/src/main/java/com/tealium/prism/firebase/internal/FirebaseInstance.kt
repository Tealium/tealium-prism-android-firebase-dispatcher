package com.tealium.prism.firebase.internal

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Default [FirebaseAnalyticsInterface] backed by [FirebaseAnalytics.getInstance].
 *
 * Firebase must already be initialized when the first call is made — either through the
 * automatic content-provider bootstrap from the `firebase-analytics` dependency, or via
 * an explicit `FirebaseApp.initializeApp(context)` call in the host application.
 */
internal class FirebaseInstance(context: Context) : FirebaseAnalyticsInterface {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context.applicationContext)
    }

    override fun logEvent(name: String, parameters: Bundle?) {
        firebaseAnalytics.logEvent(name, parameters)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    override fun resetAnalyticsData() {
        firebaseAnalytics.resetAnalyticsData()
    }

    override fun setDefaultEventParameters(parameters: Bundle?) {
        firebaseAnalytics.setDefaultEventParameters(parameters)
    }

    override fun setConsent(
        consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>
    ) {
        firebaseAnalytics.setConsent(consentSettings)
    }

    override fun setSessionTimeoutDuration(milliseconds: Long) {
        firebaseAnalytics.setSessionTimeoutDuration(milliseconds)
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
}
