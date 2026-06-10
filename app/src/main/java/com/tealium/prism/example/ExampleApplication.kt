package com.tealium.prism.example

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.Tealium
import com.tealium.prism.core.api.TealiumConfig
import com.tealium.prism.core.api.logger.LogLevel
import com.tealium.prism.core.api.misc.TimeFrameUtils.minutes
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.FirebaseLogLevel
import com.tealium.prism.firebase.firebase

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Tealium.create(buildConfig(this), null)
    }

    companion object {
        fun buildConfig(application: Application): TealiumConfig =
            TealiumConfig.Builder(
                application = application,
                accountName = "tealiummobile",
                profileName = "firebase-test",
                environment = "dev",
                modules = emptyList(),
            )
                // .setSettingsFile("tealium-settings.json")
                .addModule(firebaseModule())
                .configureCoreSettings { it.setLogLevel(LogLevel.TRACE) }
                .build()

        private fun firebaseModule() = Modules.firebase { builder ->
            builder
                .setSessionTimeout(30.minutes)
                .setAnalyticsEnabled(true)
                .setLogLevel(FirebaseLogLevel.DEBUG)
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
}
