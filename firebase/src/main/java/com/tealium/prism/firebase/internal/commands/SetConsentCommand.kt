package com.tealium.prism.firebase.internal.commands

import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.command.Command
import com.tealium.prism.core.api.command.CommandException
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.internal.ConsentConverter
import com.tealium.prism.firebase.internal.FirebaseAnalyticsInterface

/**
 * Configures Firebase Analytics consent state.
 *
 * Unknown consent type/status strings cause the command to fail — on Android the SDK enums are
 * closed, so unrecognized values cannot be forwarded to Firebase. Failing loud surfaces
 * configuration mistakes instead of silently discarding the entry.
 *
 * Known types: `ad_storage`, `analytics_storage`, `ad_user_data`, `ad_personalization`.
 * Known values: `granted`, `denied`.
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
    Command.synchronous(FirebaseCommand.SET_CONSENT.commandName) { payload ->
        val path = FirebaseDestination.ConsentSettings.asJsonObjectPath()
        val consentDict = payload.extractDataObject(path)
            ?: throw CommandException.missingParameter(path.toString())

        val settings = mutableMapOf<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>()
        for ((key, value) in consentDict) {
            val rawStatus = value.getString()
                ?: throw CommandException.invalidParameterType(
                    "${path}.$key",
                    "string consent status",
                )
            val type = ConsentConverter.typeOrNull(key)
                ?: throw CommandException.invalidParameterType(
                    "${path}.$key",
                    "known consent type (ad_storage, analytics_storage, ad_user_data, ad_personalization)",
                )
            val status = ConsentConverter.statusOrNull(rawStatus)
                ?: throw CommandException.invalidParameterType(
                    "${path}.$key=$rawStatus",
                    "known consent status (granted, denied)",
                )
            settings[type] = status
        }

        if (settings.isEmpty()) {
            throw CommandException.noValidParameters()
        }

        firebaseInstance.setConsent(settings)
    }
