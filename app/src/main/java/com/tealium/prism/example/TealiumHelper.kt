package com.tealium.prism.example

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.Tealium
import com.tealium.prism.core.api.TealiumConfig
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.logger.LogLevel
import com.tealium.prism.core.api.misc.TimeFrameUtils.minutes
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.firebase

/**
 * Centralizes all Tealium SDK interaction for the example app.
 *
 * Owns the single [Tealium] instance and exposes a small surface — [init], [track],
 * [shutdown] — so UI code never touches the SDK directly. This mirrors the recommended
 * integration pattern used across Tealium sample apps.
 */
object TealiumHelper {

    private const val INSTANCE_KEY = "tealiummobile-firebase-test"

    var shared: Tealium? = null
        private set

    fun init(application: Application) {
        shared = Tealium.create(buildConfig(application)) { result ->
            result.getOrNull()?.let { shared = it }
        }
    }

    fun track(event: String, data: DataObject = DataObject.create {}) {
        shared?.track(event, data)
    }

    fun shutdown() {
        Tealium.shutdown(INSTANCE_KEY)
        shared = null
    }

    private fun buildConfig(application: Application): TealiumConfig =
        TealiumConfig.Builder(
            application = application,
            accountName = "tealiummobile",
            profileName = "firebase-test",
            environment = "dev",
            modules = emptyList(),
        )
            // .setSettingsFile("tealium-settings.json")
            .addModule(firebaseModule())
            .configureCoreSettings { it.setLogLevel(LogLevel.DEBUG) }
            .build()

    private fun firebaseModule() = Modules.firebase { builder ->
        builder
            .setSessionTimeout(30.minutes)
            .setAnalyticsEnabled(true)
            .setMappings {
                mapCommand(FirebaseCommand.LOG_EVENT).ifValueEquals("command_name", "logevent")
                mapFrom("tealium_event", FirebaseDestination.EventName)
                mapFrom("event_name", FirebaseDestination.EventName)
                mapFrom("total", FirebaseDestination.EventParam(Param.VALUE))
                mapFrom("currency", FirebaseDestination.EventParam(Param.CURRENCY))
                mapFrom("transaction_id", FirebaseDestination.EventParam(Param.TRANSACTION_ID))
                mapFrom("product_ids", FirebaseDestination.ItemParam(Param.ITEM_ID))
                mapFrom("product_names", FirebaseDestination.ItemParam(Param.ITEM_NAME))
                mapFrom("prices", FirebaseDestination.ItemParam(Param.PRICE))
                mapFrom("quantities", FirebaseDestination.ItemParam(Param.QUANTITY))
                mapFrom("items_aoo", FirebaseDestination.EventParam(Param.ITEMS))

                mapCommand(FirebaseCommand.SET_USER_ID).ifValueEquals("command_name", "setuserid")
                mapFrom("customer_id", FirebaseDestination.UserId)

                mapCommand(FirebaseCommand.SET_USER_PROPERTY).ifValueEquals("command_name", "setuserproperty")
                mapFrom("property_name", FirebaseDestination.UserPropertyName)
                mapFrom("property_value", FirebaseDestination.UserPropertyValue)

                mapCommand(FirebaseCommand.SET_CONSENT).ifValueEquals("command_name", "setconsent")
                mapFrom("consent_settings", FirebaseDestination.ConsentSettings)

                mapCommand(FirebaseCommand.RESET_DATA).ifValueEquals("command_name", "resetdata")

                mapCommand(FirebaseCommand.SET_DEFAULT_PARAMETERS).ifValueEquals("command_name", "setdefaultparameters")
                mapFrom("parameters", FirebaseDestination.DefaultParams)

                mapCommand(FirebaseCommand.SET_SESSION_TIMEOUT).ifValueEquals("command_name", "setsessiontimeout")
                mapFrom("session_timeout_seconds", FirebaseDestination.SessionTimeout)

                mapCommand(FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED).ifValueEquals("command_name", "setanalyticscollectionenabled")
                mapFrom("analytics_collection_enabled", FirebaseDestination.AnalyticsEnabled)
            }
    }
}
