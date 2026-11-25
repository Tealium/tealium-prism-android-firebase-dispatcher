package com.tealium.prism.firebase.internal.commands

import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.data.ExtractionException
import com.tealium.prism.core.api.data.requireDataObject
import com.tealium.prism.core.api.data.requireOneOf
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.internal.converters.ConsentConverter
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface

/**
 * Configures Firebase Analytics consent state.
 *
 * Unknown consent type/status strings cause the command to fail — on Android the SDK enums are
 * closed, so unrecognized values cannot be forwarded to Firebase. Failing loud surfaces
 * configuration mistakes instead of silently discarding the entry.
 *
 * Known types: `ad_storage`, `analytics_storage`, `ad_user_data`, `ad_personalization`.
 * Known values: `granted`, `denied`. Both are matched ignoring case, and values are trimmed.
 *
 * Expected payload:
 * ```json
 * {
 *   "command_name": "setconsent",
 *   "consent_settings": {
 *     "ad_storage": "granted",
 *     "analytics_storage": "granted",
 *     "ad_user_data": "denied",
 *     "ad_personalization": "denied"
 *   }
 * }
 * ```
 */
internal fun setConsentCommand(firebaseInstance: FirebaseAnalyticsInterface): Command =
    Command.synchronous(FirebaseCommand.SET_CONSENT) { payload ->
        val path = FirebaseDestination.ConsentSettings.asJsonObjectPath()
        val consentDict = payload.requireDataObject(path)
        if (consentDict.size == 0) {
            throw ExtractionException.Empty(path.toString())
        }

        val settings = mutableMapOf<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>()
        for ((key, _) in consentDict) {
            val settingPath = path.key(key)
            val type = ConsentConverter.typeOrNull(key)
                ?: throw ExtractionException.InvalidValue(
                    settingPath.toString(),
                    "unknown consent type, expected one of ${ConsentConverter.typeNames}",
                )
            settings[type] = payload.requireOneOf(settingPath, ConsentConverter.statuses)
        }

        firebaseInstance.setConsent(settings)
    }
