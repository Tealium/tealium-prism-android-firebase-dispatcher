package com.tealium.prism.firebase.internal.converters

import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType

/**
 * Bidirectional mapping between consent string values and the Firebase Android enums.
 *
 * Strings match the iOS `ConsentType`/`ConsentStatus` raw values used throughout the
 * cross-platform payload schema. Unknown inputs return `null`; callers decide how to react —
 * `setConsentCommand` fails the command, since the Android SDK enums are closed and an
 * unrecognized value cannot be forwarded to Firebase.
 */
internal object ConsentConverter {

    private val typeByRaw: Map<String, ConsentType> = mapOf(
        "ad_storage" to ConsentType.AD_STORAGE,
        "analytics_storage" to ConsentType.ANALYTICS_STORAGE,
        "ad_user_data" to ConsentType.AD_USER_DATA,
        "ad_personalization" to ConsentType.AD_PERSONALIZATION,
    )

    private val rawByType: Map<ConsentType, String> =
        typeByRaw.entries.associate { (raw, type) -> type to raw }

    /**
     * The consent statuses keyed by their raw value, for use with `DataObject.requireOneOf`.
     */
    val statuses: Map<String, ConsentStatus> = mapOf(
        "granted" to ConsentStatus.GRANTED,
        "denied" to ConsentStatus.DENIED,
    )

    /** The known raw consent types, comma separated and sorted, for error messages. */
    val typeNames: String = typeByRaw.keys.sorted().joinToString(", ")

    fun typeOrNull(raw: String): ConsentType? =
        typeByRaw[raw.lowercase()]


    fun rawValue(type: ConsentType): String =
        rawByType[type] ?: type.name.lowercase()
}
