package com.tealium.prism.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun DemoButtons(modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    var tealiumStarted by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // MARK: - LogEvent Command
        SectionHeader("LogEvent Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::trackPurchase) { Text("Track Purchase Event") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::trackPurchaseLogEvent) { Text("Track Purchase Event (log_event)") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::trackPurchaseArrayOfObjects) { Text("Track Purchase Event (array-of-objects)") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetUserId Command
        SectionHeader("SetUserId Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::setUserId) { Text("Set User ID") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::clearUserId) { Text("Clear User ID") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetUserProperty Command
        SectionHeader("SetUserProperty Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::setUserProperty) { Text("Set Property") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::clearUserProperty) { Text("Clear Property") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetUserProperties Command
        SectionHeader("SetUserProperties Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::setMultipleUserProperties) { Text("Set Multiple Properties") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::clearMultipleUserProperties) { Text("Clear Multiple Properties") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetDefaultParameters Command
        SectionHeader("SetDefaultParameters Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::setDefaultParameters) { Text("Set Default Params") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::clearDefaultParameters) { Text("Clear Default Params") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetConsent Command
        SectionHeader("SetConsent Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::grantAllConsent) { Text("Grant All Consent") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::denyAllConsent) { Text("Deny All Consent") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - ResetData Command
        SectionHeader("ResetData Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::resetAnalyticsData) { Text("Reset Firebase Data") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetSessionTimeout Command
        SectionHeader("SetSessionTimeout Command")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::updateSessionTimeout) { Text("Set to 1 Hour") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::resetSessionTimeout) { Text("Reset to Default (30 min)") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - SetAnalyticsCollectionEnabled Command
        SectionHeader("SetAnalyticsCollectionEnabled")
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::toggleAnalyticsOn) { Text("Enable Analytics") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = ::toggleAnalyticsOff) { Text("Disable Analytics") }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MARK: - Developer Tools
        SectionHeader("Developer Tools")
        val context = androidx.compose.ui.platform.LocalContext.current
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (tealiumStarted) {
                    Tealium.shutdown("tealiummobile-firebase-test")
                } else {
                    Tealium.create(
                        ExampleApplication.buildConfig(context.applicationContext as android.app.Application),
                        null
                    )
                }
                tealiumStarted = !tealiumStarted
            }
        ) {
            Text(if (tealiumStarted) "Stop Tealium" else "Start Tealium")
        }
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

private fun trackPurchaseLogEvent() = track(
    "log_event",
    DataObject.create {
        put("command_name", "logevent")
        put("event_name", "purchase")
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

private fun clearDefaultParameters() = track(
    "set_default_params",
    DataObject.create {
        put("command_name", "setdefaultparameters")
    }
)

private fun clearUserProperty() = track(
    "set_user_property",
    DataObject.create {
        put("command_name", "setuserproperty")
        put("property_name", "user_tier")
        put("property_value", "")
    }
)

private fun setMultipleUserProperties() = track(
    "set_user_property",
    DataObject.create {
        put("command_name", "setuserproperty")
        put("property_name", DataList.fromStringCollection(listOf("tier", "level", "status")))
        put("property_value", DataList.fromStringCollection(listOf("premium", "expert", "active")))
    }
)

private fun clearMultipleUserProperties() = track(
    "set_user_property",
    DataObject.create {
        put("command_name", "setuserproperty")
        put("property_name", DataList.fromStringCollection(listOf("tier", "level", "status")))
        put("property_value", DataList.fromStringCollection(listOf("", "", "")))
    }
)

private fun resetSessionTimeout() = track(
    "update_session_timeout",
    DataObject.create {
        put("session_timeout_seconds", 1800)
        put("command_name", "setsessiontimeout")
    }
)

private fun toggleAnalyticsOn() = track(
    "toggle_analytics",
    DataObject.create {
        put("analytics_collection_enabled", true)
        put("command_name", "setanalyticscollectionenabled")
    }
)

private fun trackPurchaseArrayOfObjects() = track(
    "purchase",
    DataObject.create {
        put("command_name", "logevent")
        put("event_name", "purchase")
        put("total", 249.97)
        put("currency", "USD")
        put("items_aoo", DataList.create {
            add(DataObject.create {
                put("item_id", "SKU-001")
                put("item_name", "Premium Widget")
                put("price", 99.99)
                put("quantity", 1)
            })
            add(DataObject.create {
                put("item_id", "SKU-002")
                put("item_name", "Gadget Pro")
                put("price", 79.99)
                put("quantity", 2)
            })
        })
    }
)
