package com.tealium.prism.firebase

import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.misc.TimeFrame
import com.tealium.prism.core.api.misc.TimeFrameUtils.inSeconds
import com.tealium.prism.core.api.settings.modules.DispatcherSettingsBuilder
import com.tealium.prism.firebase.internal.FirebaseDispatcherConfiguration

/**
 * Builder for Firebase Dispatcher configuration settings.
 *
 * Configures Firebase Analytics behavior at initialization. For data mappings,
 * use [setMappings] with [FirebaseMappings] type-safe enums.
 *
 * ```kotlin
 * Modules.firebase {
 *     it.setSessionTimeout(30.minutes)
 *       .setAnalyticsEnabled(true)
 *       .setMappings {
 *           mapCommand(FirebaseCommand.LOG_EVENT)
 *           mapFrom("tealium_event", FirebaseDestination.EventName)
 *       }
 * }
 * ```
 */
class FirebaseSettingsBuilder :
    DispatcherSettingsBuilder<FirebaseMappings, FirebaseSettingsBuilder>(
        Modules.Types.FIREBASE,
        ::FirebaseMappings,
    ) {

    /**
     * Set the session timeout duration.
     *
     * Configures how long a session lasts before timing out. If not set, the value is left
     * untouched on the SDK and Firebase applies its own default. The Android SDK accepts
     * milliseconds; seconds are preserved in the payload and converted when applied to the SDK.
     */
    fun setSessionTimeout(timeout: TimeFrame) = apply {
        configuration.put(FirebaseDispatcherConfiguration.KEY_SESSION_TIMEOUT, timeout.inSeconds())
    }

    /**
     * Enable or disable analytics collection.
     *
     * When disabled, no analytics data will be collected or sent to Firebase.
     */
    fun setAnalyticsEnabled(enabled: Boolean) = apply {
        configuration.put(FirebaseDispatcherConfiguration.KEY_ANALYTICS_ENABLED, enabled)
    }
}
