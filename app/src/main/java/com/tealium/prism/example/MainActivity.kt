package com.tealium.prism.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tealium.prism.core.api.Tealium
import com.tealium.prism.core.api.data.DataList
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.example.ui.theme.ExampleTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    DemoButtons(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun DemoButtons(modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Firebase Dispatcher demo")

        Button(onClick = ::trackPurchase) { Text("Track purchase event") }
        Button(onClick = ::setUserId) { Text("Set user id") }
        Button(onClick = ::clearUserId) { Text("Clear user id") }
        Button(onClick = ::setUserProperty) { Text("Set user property") }
        Button(onClick = ::grantAllConsent) { Text("Grant all consent") }
        Button(onClick = ::denyAllConsent) { Text("Deny all consent") }
        Button(onClick = ::resetAnalyticsData) { Text("Reset analytics data") }
        Button(onClick = ::updateSessionTimeout) { Text("Update session timeout") }
        Button(onClick = ::toggleAnalyticsOff) { Text("Disable analytics") }
        Button(onClick = ::setDefaultParameters) { Text("Set default parameters") }
    }
}

private fun track(event: String, data: DataObject = DataObject.create {}) {
    Tealium.get("tealiummobile-firebase-test") { instance ->
        instance?.track(event, data)
    }
}

private fun trackPurchase() = track(
    "purchase",
    DataObject.create {
        put("command_name", "logevent")
        put("tealium_event", "purchase")
        put("total", 249.97)
        put("currency", "USD")
        put("transaction_id", "TXN-2026-001")
        put("product_ids", DataList.fromStringCollection(listOf("SKU-001", "SKU-002", "SKU-003")))
        put("product_names", DataList.fromStringCollection(listOf("Widget", "Gadget", "Tool")))
        put("prices", DataList.fromDoubleCollection(listOf(99.99, 79.99, 69.99)))
        put("quantities", DataList.fromIntCollection(listOf(1, 2, 1)))
    }
)

private fun setUserId() = track(
    "set_user_id",
    DataObject.create {
        put("command_name", "setuserid")
        put("customer_id", "user_12345")
    }
)

private fun clearUserId() = track(
    "set_user_id",
    DataObject.create {
        put("command_name", "setuserid")
        put("customer_id", "")
    }
)

private fun setUserProperty() = track(
    "set_user_property",
    DataObject.create {
        put("command_name", "setuserproperty")
        put("property_name", "user_tier")
        put("property_value", "premium")
    }
)

private fun grantAllConsent() = track(
    "update_consent",
    DataObject.create {
        put(
            "consent_settings",
            DataObject.create {
                put("ad_storage", "granted")
                put("analytics_storage", "granted")
                put("ad_user_data", "granted")
                put("ad_personalization", "granted")
            },
        )
        put("command_name", "setconsent")
    }
)

private fun denyAllConsent() = track(
    "update_consent",
    DataObject.create {
        put(
            "consent_settings",
            DataObject.create {
                put("ad_storage", "denied")
                put("analytics_storage", "denied")
                put("ad_user_data", "denied")
                put("ad_personalization", "denied")
            },
        )
        put("command_name", "setconsent")
    }
)

private fun resetAnalyticsData() = track(
    "reset_firebase_data",
    DataObject.create { put("command_name", "resetdata") }
)

private fun updateSessionTimeout() = track(
    "update_session_timeout",
    DataObject.create {
        put("session_timeout_seconds", 3600)
        put("command_name", "setsessiontimeout")
    }
)

private fun toggleAnalyticsOff() = track(
    "toggle_analytics",
    DataObject.create {
        put("analytics_collection_enabled", false)
        put("command_name", "setanalyticscollectionenabled")
    }
)

private fun setDefaultParameters() = track(
    "set_default_params",
    DataObject.create {
        put(
            "parameters",
            DataObject.create {
                put("app_version", "1.0.0")
                put("environment", "production")
            },
        )
        put("command_name", "setdefaultparameters")
    }
)
