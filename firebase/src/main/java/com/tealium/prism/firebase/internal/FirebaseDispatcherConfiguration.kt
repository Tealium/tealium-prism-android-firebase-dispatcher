package com.tealium.prism.firebase.internal

import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.data.LenientConverters
import com.tealium.prism.firebase.FirebaseLogLevel

/**
 * Configuration values read from Firebase dispatcher settings.
 *
 * Values are applied both at initialization and whenever
 * `FirebaseDispatcher.updateConfiguration` is invoked. `null` values are left untouched
 * on the SDK — only explicitly provided settings take effect.
 */
internal data class FirebaseDispatcherConfiguration(
    val sessionTimeoutSeconds: Double?,
    val analyticsCollectionEnabled: Boolean?,
    val logLevel: FirebaseLogLevel?,
) {
    companion object {
        const val KEY_SESSION_TIMEOUT = "session_timeout_seconds"
        const val KEY_ANALYTICS_ENABLED = "analytics_collection_enabled"
        const val KEY_LOG_LEVEL = "log_level"

        fun fromDataObject(data: DataObject): FirebaseDispatcherConfiguration =
            FirebaseDispatcherConfiguration(
                sessionTimeoutSeconds = data.get(KEY_SESSION_TIMEOUT, LenientConverters.DOUBLE),
                analyticsCollectionEnabled = data.get(KEY_ANALYTICS_ENABLED, LenientConverters.BOOLEAN),
                logLevel = data.get(KEY_LOG_LEVEL, LenientConverters.STRING)
                    ?.let { FirebaseLogLevel.fromString(it) },
            )
    }
}
