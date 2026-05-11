package com.tealium.prism.firebase

import java.util.Locale

/**
 * Firebase internal log level.
 *
 * Cross-platform parity with the iOS `FirebaseLogLevel`. Seven case names are exposed as distinct
 * values for clarity — iOS's `FirebaseLoggerLevel` treats `.min`/`.error` and `.max`/`.debug` as
 * aliases, but the dispatcher schema keeps them separate.
 *
 * Android's Firebase Analytics SDK does not expose log level configuration via the
 * `FirebaseAnalytics` class, so this setting is a no-op at runtime on Android. It is accepted to
 * keep the configuration payload consistent across platforms.
 */
enum class FirebaseLogLevel(val rawValue: String) {
    MIN("min"),
    ERROR("error"),
    WARNING("warning"),
    NOTICE("notice"),
    INFO("info"),
    DEBUG("debug"),
    MAX("max");

    companion object {
        /**
         * Returns the [FirebaseLogLevel] matching [value] (case-insensitive), or `null` if none.
         */
        fun fromString(value: String): FirebaseLogLevel? {
            val normalized = value.lowercase(Locale.ROOT)
            return entries.firstOrNull { it.rawValue == normalized }
        }
    }
}
