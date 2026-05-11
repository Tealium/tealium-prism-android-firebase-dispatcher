package com.tealium.prism.firebase.internal

import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import java.util.Locale

/**
 * Bidirectional mapping between consent string values and the Firebase Android enums.
 *
 * Strings match the iOS `ConsentType`/`ConsentStatus` raw values used throughout the
 * cross-platform payload schema. Unknown inputs return `null` — per spec, unrecognized
 * values are not forwarded to Firebase on Android.
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

    private val statusByRaw: Map<String, ConsentStatus> = mapOf(
        "granted" to ConsentStatus.GRANTED,
        "denied" to ConsentStatus.DENIED,
    )

    fun typeOrNull(raw: String): ConsentType? =
        typeByRaw[raw.lowercase(Locale.ROOT)]

    fun statusOrNull(raw: String): ConsentStatus? =
        statusByRaw[raw.lowercase(Locale.ROOT)]

    fun rawValue(type: ConsentType): String =
        rawByType[type] ?: type.name.lowercase(Locale.ROOT)
}
