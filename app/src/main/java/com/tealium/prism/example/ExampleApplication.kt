package com.tealium.prism.example

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.Tealium
import com.tealium.prism.core.api.TealiumConfig
import com.tealium.prism.core.api.misc.TimeFrameUtils.minutes
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.FirebaseLogLevel
import com.tealium.prism.firebase.firebase

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = TealiumConfig.Builder(
            application = this,
            accountName = "tealiummobile",
            profileName = "firebase-test",
            environment = "dev",
            modules = emptyList(),
        )
            // .setSettingsFile("tealium-settings.json")
            .addModule(firebaseModule())
            .build()

        Tealium.create(config, null)
    }

    private fun firebaseModule() = Modules.firebase { builder ->
        builder
            .setSessionTimeout(30.minutes)
            .setAnalyticsEnabled(true)
            .setLogLevel(FirebaseLogLevel.DEBUG)
            .setMappings {
                mapCommand(FirebaseCommand.LOG_EVENT)
                mapFrom("tealium_event", FirebaseDestination.EventName)
                mapFrom("total", FirebaseDestination.EventParam(Param.VALUE))
                mapFrom("currency", FirebaseDestination.EventParam(Param.CURRENCY))
                mapFrom("transaction_id", FirebaseDestination.EventParam(Param.TRANSACTION_ID))
                mapFrom("product_ids", FirebaseDestination.ItemParam(Param.ITEM_ID))
                mapFrom("product_names", FirebaseDestination.ItemParam(Param.ITEM_NAME))
                mapFrom("prices", FirebaseDestination.ItemParam(Param.PRICE))
                mapFrom("quantities", FirebaseDestination.ItemParam(Param.QUANTITY))

                mapCommand(FirebaseCommand.SET_USER_ID)
                mapFrom("customer_id", FirebaseDestination.UserId)

                mapCommand(FirebaseCommand.SET_USER_PROPERTY)
                mapFrom("property_name", FirebaseDestination.UserPropertyName)
                mapFrom("property_value", FirebaseDestination.UserPropertyValue)

                mapCommand(FirebaseCommand.SET_CONSENT)

                mapCommand(FirebaseCommand.RESET_DATA)

                mapCommand(FirebaseCommand.SET_DEFAULT_PARAMETERS)
                mapFrom("parameters", FirebaseDestination.DefaultParams)

                mapCommand(FirebaseCommand.SET_SESSION_TIMEOUT)
                mapFrom("session_timeout_seconds", FirebaseDestination.SessionTimeout)

                mapCommand(FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED)
                mapFrom("analytics_collection_enabled", FirebaseDestination.AnalyticsEnabled)
            }
    }
}
