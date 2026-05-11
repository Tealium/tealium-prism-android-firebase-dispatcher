# Tealium Prism Firebase Dispatcher for Android

Command Dispatcher that routes Tealium tracking events to the Firebase Analytics Android SDK.

Full schema, command list, and cross-platform semantics are documented on Confluence:
[Firebase Dispatcher](https://tealium.atlassian.net/wiki/spaces/MOB/pages/5903974885/Firebase+Dispatcher).

## Requirements

| Dependency                 | Version |
|----------------------------|---------|
| Android API                | 23+     |
| Kotlin                     | 2.0+    |
| Tealium Prism Core         | dev     |
| Firebase Analytics (BoM)   | 34.6.0+ |

## Installation

Add the Firebase Google Services plugin and the dispatcher dependency in your app module:

```kotlin
// build.gradle.kts (module: app)
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.tealium.prism:prism-bom:<version>"))
    implementation("com.tealium.prism:prism-core")
    implementation("com.tealium.prism:firebase:<version>")

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
}
```

Place `google-services.json` in the app module root.

Firebase is initialized automatically by the `firebase-analytics` content provider; no explicit
`Firebase.initialize(context)` is required unless another Firebase product needs custom ordering.

## Quick Start

```kotlin
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
            accountName = "myaccount",
            profileName = "myprofile",
            environment = "dev",
            modules = emptyList(),
        )
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
                mapFrom("product_ids", FirebaseDestination.ItemParam(Param.ITEM_ID))
                mapFrom("product_names", FirebaseDestination.ItemParam(Param.ITEM_NAME))

                mapCommand(FirebaseCommand.SET_USER_ID)
                mapFrom("customer_id", FirebaseDestination.UserId)

                mapCommand(FirebaseCommand.SET_USER_PROPERTY)
                mapFrom("prop_name", FirebaseDestination.UserPropertyName)
                mapFrom("prop_value", FirebaseDestination.UserPropertyValue)

                mapCommand(FirebaseCommand.SET_CONSENT)
            }
    }
}
```

## Supported Commands

| Command                          | Firebase API                       |
|----------------------------------|------------------------------------|
| `logevent`                       | `logEvent(name, Bundle)`           |
| `setuserid`                      | `setUserId(id)`                    |
| `setuserproperty`                | `setUserProperty(name, value)`     |
| `resetdata`                      | `resetAnalyticsData()`             |
| `setdefaultparameters`           | `setDefaultEventParameters(Bundle)`|
| `setconsent`                     | `setConsent(Map<Type, Status>)`    |
| `setsessiontimeout`              | `setSessionTimeoutDuration(ms)`    |
| `setanalyticscollectionenabled`  | `setAnalyticsCollectionEnabled()`  |

`initiateconversionmeasurement` is iOS only — the Firebase Android SDK does not expose the
on-device conversion measurement API.

See the Confluence page for the full payload schema, reserved event names, consent type/status
mappings, and the `items` array-of-objects / parallel-arrays formats.

## Configuration

| Key                              | Type   | Default            | Notes                              |
|----------------------------------|--------|--------------------|------------------------------------|
| `session_timeout_seconds`        | Number | 1800               | Seconds. Converted to ms internally. |
| `analytics_collection_enabled`   | Bool   | `true`             |                                    |
| `log_level`                      | String | Firebase default   | No-op on Android — accepted for cross-platform parity. |

## Example App

The `app` module contains a Compose demo that exercises every command. Drop a real
`google-services.json` into `app/` and run `./gradlew :app:installDebug`. Firebase DebugView will
show the forwarded events.

## Testing

```bash
./gradlew :firebase:test
```

Unit tests run on Robolectric — no device or emulator required.

## License

See [LICENSE](LICENSE).
