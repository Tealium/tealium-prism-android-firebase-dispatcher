# Tealium Prism Firebase Dispatcher for Android

Command Dispatcher that routes Tealium Prism tracking events to the Firebase Analytics Android SDK — events, user properties, consent settings, and more.

> **Important:** Firebase Analytics only supports a single shared instance. Only one Firebase dispatcher can be active at a time in your app.

## Requirements

| Dependency               | Version  |
|--------------------------|----------|
| Android API              | 23+      |
| Kotlin                   | 2.0+     |
| Tealium Prism Core       | 0.5.0+   |
| Firebase Analytics (BoM) | 34.6.0+  |

## Installation

Declare the repositories in `settings.gradle.kts`. The Google Services plugin marker is published only
to Google's Maven repository, so `google()` is required under `pluginManagement` too:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.tealiumiq.com/android/releases/") }
    }
}
```

Then add the Google Services plugin and the dependencies in your app module:

```kotlin
// build.gradle.kts (module: app)
plugins {
    id("com.google.gms.google-services") version "4.4.2"
}

dependencies {
    implementation("com.tealium.prism:firebase:1.0.0")

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
}
```

`prism-core` arrives transitively with the dispatcher. `firebase-analytics` must be declared
explicitly, as shown above: the dispatcher pulls it in at runtime but not onto your compile classpath,
and the mapping API references Firebase SDK types directly — `FirebaseDestination.ConsentSetting`
takes a `FirebaseAnalytics.ConsentType`, and the examples below use `FirebaseAnalytics.Param`
constants. Declaring the BoM yourself also keeps the Firebase SDK version under your control.

### Firebase Setup

Place `google-services.json` in the app module root. Firebase is initialized automatically by the
`firebase-analytics` content provider, so no explicit call is required. If another Firebase product
needs a specific initialization order, call `FirebaseApp.initializeApp(context)` in
`Application.onCreate()` before `Tealium.create(...)`. The dispatcher obtains its instance through
`FirebaseAnalytics.getInstance(context)` and requires Firebase to be initialized before the first
command executes.

## Example App

The `app` module contains a Compose demo that exercises every command. Drop a real
`google-services.json` into `app/` and run `./gradlew :app:installDebug`. Firebase DebugView will
show the forwarded events.

The example app demonstrates:
- Automatic Firebase initialization
- Every Firebase command with real-world use cases
- Purchase events with items and custom parameters
- User ID and user property management
- Consent settings (grant/deny all)

## Quick Start

Register the Firebase dispatcher when initializing Tealium Prism:

```kotlin
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.Tealium
import com.tealium.prism.core.api.TealiumConfig
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.misc.TimeFrameUtils.minutes
import com.tealium.prism.firebase.FirebaseCommand
import com.tealium.prism.firebase.FirebaseDestination
import com.tealium.prism.firebase.firebase

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = TealiumConfig.Builder(
            application = this,
            accountName = "my_account",
            profileName = "my_profile",
            environment = "prod",
            modules = listOf(firebaseModule()),
        ).build()

        Tealium.create(config)
    }

    private fun firebaseModule() = Modules.firebase { builder ->
        builder
            .setSessionTimeout(30.minutes)
            .setAnalyticsEnabled(true)
            .setMappings {
                mapCommand(FirebaseCommand.LOG_EVENT).forAllEvents()
                mapFrom("tealium_event", FirebaseDestination.EventName)
                mapFrom("total", FirebaseDestination.EventParam(Param.VALUE))
                mapFrom("currency", FirebaseDestination.EventParam(Param.CURRENCY))
            }
    }
}
```

The same setup in Java. `Modules.firebase { … }` is a Kotlin extension function, so Java goes through
`Firebase.configure(…)` instead:

```java
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.tealium.prism.core.api.Tealium;
import com.tealium.prism.core.api.TealiumConfig;
import com.tealium.prism.core.api.misc.TimeFrame;
import com.tealium.prism.core.api.modules.ModuleFactory;
import com.tealium.prism.firebase.Firebase;
import com.tealium.prism.firebase.FirebaseCommand;
import com.tealium.prism.firebase.FirebaseDestination;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TealiumConfig config = new TealiumConfig.Builder(
                this, "my_account", "my_profile", "prod",
                Collections.singletonList(firebaseModule())
        ).build();

        Tealium.create(config);
    }

    private ModuleFactory firebaseModule() {
        return Firebase.configure(builder -> builder
                .setSessionTimeout(new TimeFrame(30, TimeUnit.MINUTES))
                .setAnalyticsEnabled(true)
                .setMappings(mappings -> {
                    mappings.mapCommand(FirebaseCommand.LOG_EVENT).forAllEvents();
                    mappings.mapFrom("tealium_event", FirebaseDestination.EventName.INSTANCE);
                    mappings.mapFrom("total", new FirebaseDestination.EventParam(Param.VALUE));
                    mappings.mapFrom("currency", new FirebaseDestination.EventParam(Param.CURRENCY));
                }));
    }
}
```

## Configuration

The Firebase dispatcher can be configured via a local JSON settings file, remote settings, or
programmatically with `FirebaseSettingsBuilder`.

### Configuration Options

| Setting                      | JSON Key                       | Type               |
|------------------------------|--------------------------------|--------------------|
| Session timeout              | `session_timeout_seconds`      | Number (seconds)   |
| Analytics collection enabled | `analytics_collection_enabled` | Boolean            |

If a setting is omitted, the dispatcher leaves it untouched and Firebase applies its own default
(consult the Firebase Analytics documentation for the current defaults).

> **Note:** `session_timeout_seconds` is expressed in **seconds** to match the cross-platform payload
> schema. The Android SDK takes milliseconds, so the dispatcher converts the value before calling
> `setSessionTimeoutDuration`.
>
> There is no `log_level` setting on Android — the Firebase Android SDK has no equivalent of the iOS
> `FirebaseLoggerLevel` API. Use `adb shell setprop log.tag.FA VERBOSE` instead.

### JSON Settings

Configure the module in your local settings file (or the equivalent remote settings payload):

```json
{
    "modules": {
        "FirebaseDispatcher": {
            "module_type": "FirebaseDispatcher",
            "configuration": {
                "session_timeout_seconds": 1800,
                "analytics_collection_enabled": true
            }
        }
    }
}
```

The key under `"modules"` is the **module id**, which defaults to the module type
(`FirebaseDispatcher`). Local, remote, and programmatic settings are deep merged by that key, so it has
to match across all three sources. A mismatch produces two separate entries sharing one `module_type`;
deduplication then keeps only one of them, without a warning.

### Programmatic Configuration

Use `FirebaseSettingsBuilder` to enforce settings that cannot be overridden remotely:

```kotlin
Modules.firebase { builder ->
    builder
        .setSessionTimeout(30.minutes)
        .setAnalyticsEnabled(true)
}
```

> **Note:** Programmatic settings are deep merged onto local and remote settings and always take
> precedence. Only use them for values that must never be changed remotely.
>
> Pass `null` instead of a lambda (`Modules.firebase(null)`) to initialize the module *only* when
> local or remote settings are provided.

## Settings Builder Reference

`FirebaseSettingsBuilder` extends `DispatcherSettingsBuilder<FirebaseMappings, FirebaseSettingsBuilder>`
and provides these methods:

| Method                                               | Description                                                                   |
|------------------------------------------------------|-------------------------------------------------------------------------------|
| `setSessionTimeout(timeout: TimeFrame)`              | Session timeout (e.g. `30.minutes`)                                           |
| `setAnalyticsEnabled(enabled: Boolean)`              | Enable or disable analytics collection                                        |
| `setMappings(mappings: FirebaseMappings.() -> Unit)` | Configure data mappings                                                       |
| `setEnabled(enabled: Boolean)`                       | Permanently enable or disable the module; overrides local and remote settings |
| `setOrder(order: Int)`                               | Module initialization order; lower values first                               |
| `setRules(rules: Rule<String>)`                      | Conditional dispatch rules by load rule ID                                    |

`30.minutes` comes from `com.tealium.prism.core.api.misc.TimeFrameUtils.minutes` — import it
explicitly so it is not confused with `kotlin.time.Duration.Companion.minutes`.

## Commands

| Command                         | Firebase API                          |
|---------------------------------|---------------------------------------|
| `logevent`                      | `logEvent(name, Bundle)`              |
| `setuserid`                     | `setUserId(id)`                       |
| `setuserproperty`               | `setUserProperty(name, value)`        |
| `resetdata`                     | `resetAnalyticsData()`                |
| `setdefaultparameters`          | `setDefaultEventParameters(Bundle)`   |
| `setconsent`                    | `setConsent(Map<Type, Status>)`       |
| `setsessiontimeout`             | `setSessionTimeoutDuration(ms)`       |
| `setanalyticscollectionenabled` | `setAnalyticsCollectionEnabled(bool)` |

> `initiateconversionmeasurement` is **iOS only** — the Firebase Android SDK does not expose the
> on-device conversion measurement API.

The dispatcher routes dispatch data to Firebase using the Tealium Prism Mappings system. Each command
below includes both the JSON and the programmatic mapping configuration.

JSON mapping objects are entries in the `"mappings"` array of your module configuration. Programmatic
mappings use `FirebaseMappings` with the type-safe `FirebaseCommand` and `FirebaseDestination` enums —
`mapCommand(...)` declares which command a mapping group handles, and `mapFrom(...)` maps a source key
to a Firebase destination.

In all examples below, replace `YOUR_EVENT_NAME` with the `tealium_event` value you use in your
`tealium.track(...)` calls to trigger the command (e.g. `"user_logout"` for `resetdata`).

Firebase-reserved parameter constants (`Param.VALUE`, `Param.CURRENCY`, `Param.ITEM_ID`, …) live in
`com.google.firebase.analytics.FirebaseAnalytics.Param` and are documented in the
[Firebase Analytics event parameters reference](https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Param).

---

### Log Event

Logs an event to Firebase Analytics. Supports predefined Firebase events and custom events, with
optional parameters and nested item arrays for e-commerce.

**command_name:** `logevent`

There are two mapping approaches for this command.

#### Approach 1 — Explicit event name

`tealium_event` identifies the command type. The Firebase event name is passed as a separate
`event_name` field in the dispatch data. This is consistent with all other commands.

**Dispatch data:**
```kotlin
tealium.track(
    "log_event",
    DataObject.create {
        put("event_name", "purchase")
        put("total", 99.99)
        put("currency", "USD")
    }
)
```

**JSON Mappings:**
```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "log_event" },
        "map_to": { "value": "logevent" }
    }
},
{
    "destination": { "path": "event_name" },
    "parameters": { "reference": { "key": "event_name" } }
},
{
    "destination": { "path": "parameters.value" },
    "parameters": { "reference": { "key": "total" } }
},
{
    "destination": { "path": "parameters.currency" },
    "parameters": { "reference": { "key": "currency" } }
}
```

**Programmatic:**
```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.LOG_EVENT).ifValueEquals("tealium_event", "log_event")
    mapFrom("event_name", FirebaseDestination.EventName)
    mapFrom("total", FirebaseDestination.EventParam(Param.VALUE))
    mapFrom("currency", FirebaseDestination.EventParam(Param.CURRENCY))
}
```

#### Approach 2 — Shortcut: `tealium_event` as event name

`tealium_event` is both the trigger and the Firebase event name. No separate `event_name` field is
needed. Useful when your Tealium event names already match Firebase event names.

**Dispatch data:**
```kotlin
tealium.track(
    "purchase",
    DataObject.create {
        put("total", 99.99)
        put("currency", "USD")
    }
)
```

**JSON Mappings:**
```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "purchase" },
        "map_to": { "value": "logevent" }
    }
},
{
    "destination": { "path": "event_name" },
    "parameters": { "reference": { "key": "tealium_event" } }
},
{
    "destination": { "path": "parameters.value" },
    "parameters": { "reference": { "key": "total" } }
},
{
    "destination": { "path": "parameters.currency" },
    "parameters": { "reference": { "key": "currency" } }
}
```

**Programmatic:**
```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.LOG_EVENT).ifValueEquals("tealium_event", "purchase")
    mapFrom("tealium_event", FirebaseDestination.EventName)
    mapFrom("total", FirebaseDestination.EventParam(Param.VALUE))
    mapFrom("currency", FirebaseDestination.EventParam(Param.CURRENCY))
}
```

> With Approach 2, each Firebase event name requires its own `command_name` filter mapping. Approach 1
> uses a single `"log_event"` filter for all events. To forward *every* tracked event, replace the
> filter with `mapCommand(FirebaseCommand.LOG_EVENT).forAllEvents()`.

#### Item Formats

Items under `parameters.items` support two equivalent formats.

**Parallel arrays** (Tealium convention — each property is an array of values per item):

```json
"items": {
    "item_id":   ["SKU001", "SKU002"],
    "item_name": ["Widget", "Gadget"],
    "price":     [29.99, 70.00]
}
```

**Array of objects** (Firebase-ready format):

```json
"items": [
    { "item_id": "SKU001", "item_name": "Widget", "price": 29.99 },
    { "item_id": "SKU002", "item_name": "Gadget", "price": 70.00 }
]
```

Both formats produce identical Firebase output — an `ArrayList<Bundle>` under `Param.ITEMS`.

Mapping individual item properties uses `FirebaseDestination.ItemParam`:

```kotlin
mapFrom("product_ids", FirebaseDestination.ItemParam(Param.ITEM_ID))
mapFrom("product_names", FirebaseDestination.ItemParam(Param.ITEM_NAME))
```

Android-specific handling:
- In the parallel-arrays format, **all arrays must have the same length**; a mismatch fails the
  command with an array-length-mismatch error.
- A scalar value is treated as a single-element array, so `{"item_id": "SKU001"}` yields one item.
- In the array-of-objects format, entries that are not objects are dropped.
- In both formats, an item that resolves to no usable properties (e.g. every parallel array holds
  `null` at that index) is dropped rather than sent as an empty item. Firebase discards empty items
  anyway, so keeping them would only push the event closer to the per-event item limit.

---

### Set User ID

Associates analytics data with a specific user.

**command_name:** `setuserid`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "setuserid" }
    }
},
{
    "destination": { "path": "user_id" },
    "parameters": { "reference": { "key": "user_id" } }
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.SET_USER_ID).ifValueEquals("tealium_event", "user_login")
    mapFrom("user_id", FirebaseDestination.UserId)
}
```

> Passing an empty string clears the user ID. Use of this field must comply with Google's Privacy Policy.

---

### Set User Property

Sets one or more user properties. Properties persist across sessions.

The dispatcher forwards every resolved name/value pair to the SDK as-is. Firebase enforces its own quotas
on user property names and values — consult the Firebase Analytics documentation for the current ones.

**command_name:** `setuserproperty`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "setuserproperty" }
    }
},
{
    "destination": { "path": "property_name" },
    "parameters": { "reference": { "key": "property_name" } }
},
{
    "destination": { "path": "property_value" },
    "parameters": { "reference": { "key": "property_value" } }
}
```

To set multiple properties in a single dispatch, map source keys whose values are arrays. Both arrays
must have the same length — each index pairs one name with one value:

```json
{
    "property_name": ["subscription_tier", "user_level"],
    "property_value": ["premium", "expert"]
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.SET_USER_PROPERTY).ifValueEquals("tealium_event", "profile_update")
    mapFrom("property_name", FirebaseDestination.UserPropertyName)
    mapFrom("property_value", FirebaseDestination.UserPropertyValue)
}
```

> Passing an empty (or null) `property_value` removes that property from Firebase. A length mismatch
> between the name and value arrays fails the command, as does a name array containing only nulls.

---

### Set Default Parameters

Sets parameters that are automatically appended to every subsequent event. Useful for app-wide context
such as version or locale. Defaults persist across app runs and have lower precedence than
event-level parameters.

**command_name:** `setdefaultparameters`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "setdefaultparameters" }
    }
},
{
    "destination": { "path": "parameters.version" },
    "parameters": { "reference": { "key": "app_version" } }
},
{
    "destination": { "path": "parameters.language" },
    "parameters": { "reference": { "key": "app_language" } }
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.SET_DEFAULT_PARAMETERS).ifValueEquals("tealium_event", "app_launch")
    mapFrom("app_version", FirebaseDestination.DefaultParam("version"))
    mapFrom("app_language", FirebaseDestination.DefaultParam("language"))
}
```

> **Clearing defaults:** the `parameters` key must be **absent entirely** from the mapped payload —
> then the dispatcher calls `setDefaultEventParameters(null)` and Firebase drops all defaults. An
> empty `parameters` object is a deliberate no-op, so a partially-populated dispatch can never wipe
> your defaults by accident.
>
> Map a dedicated event to the command and dispatch it with none of the parameter source keys present:
> ```json
> {
>     "destination": { "key": "command_name" },
>     "parameters": {
>         "reference": { "key": "tealium_event" },
>         "filter": { "value": "clear_defaults" },
>         "map_to": { "value": "setdefaultparameters" }
>     }
> }
> ```
> ```kotlin
> tealium.track("clear_defaults", DataObject.create {})
> ```
> Because the dispatch carries none of the `parameters.*` source keys (`app_version`,
> `app_language`), no parameters resolve and Firebase clears all default event parameters.

---

### Set Session Timeout

Overrides the session timeout at runtime, replacing whatever was applied at initialization.

**command_name:** `setsessiontimeout`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "setsessiontimeout" }
    }
},
{
    "destination": { "path": "session_timeout_seconds" },
    "parameters": { "reference": { "key": "session_timeout_seconds" } }
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.SET_SESSION_TIMEOUT).ifValueEquals("tealium_event", "session_config")
    mapFrom("session_timeout_seconds", FirebaseDestination.SessionTimeout)
}
```

> The payload value is in seconds; the dispatcher converts it to milliseconds for
> `setSessionTimeoutDuration`.

---

### Set Analytics Collection Enabled

Enables or disables Firebase Analytics data collection at runtime. When disabled, collection stops but
previously collected data is retained.

**command_name:** `setanalyticscollectionenabled`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "setanalyticscollectionenabled" }
    }
},
{
    "destination": { "path": "analytics_collection_enabled" },
    "parameters": { "reference": { "key": "analytics_enabled" } }
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.SET_ANALYTICS_COLLECTION_ENABLED)
        .ifValueEquals("tealium_event", "privacy_update")
    mapFrom("analytics_enabled", FirebaseDestination.AnalyticsEnabled)
}
```

---

### Set Consent

Configures end-user consent state for device identifiers. Should be applied before logging any events.

**command_name:** `setconsent`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "setconsent" }
    }
},
{
    "destination": { "path": "consent_settings.ad_storage" },
    "parameters": { "reference": { "key": "ad_storage_consent" } }
},
{
    "destination": { "path": "consent_settings.analytics_storage" },
    "parameters": { "reference": { "key": "analytics_storage_consent" } }
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.SET_CONSENT).ifValueEquals("tealium_event", "consent_update")
    mapFrom("ad_storage_consent", FirebaseDestination.ConsentSetting(ConsentType.AD_STORAGE))
    mapFrom(
        "analytics_storage_consent",
        FirebaseDestination.ConsentSetting(ConsentType.ANALYTICS_STORAGE),
    )
}
```

`ConsentType` is `com.google.firebase.analytics.FirebaseAnalytics.ConsentType`.

#### Consent Types

| Consent Type         | `FirebaseDestination`                              | Status Values        |
|----------------------|----------------------------------------------------|----------------------|
| `ad_storage`         | `ConsentSetting(ConsentType.AD_STORAGE)`           | `granted`, `denied`  |
| `analytics_storage`  | `ConsentSetting(ConsentType.ANALYTICS_STORAGE)`    | `granted`, `denied`  |
| `ad_user_data`       | `ConsentSetting(ConsentType.AD_USER_DATA)`         | `granted`, `denied`  |
| `ad_personalization` | `ConsentSetting(ConsentType.AD_PERSONALIZATION)`   | `granted`, `denied`  |

> Type and status strings are matched case-insensitively. An unknown consent type or status fails the
> command — the Android SDK enums are closed, so an unrecognized value cannot be forwarded, and failing
> loud surfaces the configuration mistake instead of silently dropping the entry. A `consent_settings`
> object that resolves to no valid entries also fails.

---

### Reset Data

Clears all analytics data for this app instance from the device and resets the app instance ID. Use on
user logout or when privacy regulations require data deletion.

**command_name:** `resetdata`

#### JSON Mappings

```json
{
    "destination": { "key": "command_name" },
    "parameters": {
        "reference": { "key": "tealium_event" },
        "filter": { "value": "YOUR_EVENT_NAME" },
        "map_to": { "value": "resetdata" }
    }
}
```

#### Programmatic

```kotlin
builder.setMappings {
    mapCommand(FirebaseCommand.RESET_DATA).ifValueEquals("tealium_event", "user_logout")
}
```

## API Reference

Generated KDoc for the public API is published at
[tealium.github.io/tealium-prism-android-firebase-dispatcher](https://tealium.github.io/tealium-prism-android-firebase-dispatcher/).

Build it locally with:

```bash
./gradlew :firebase:dokkaGenerate
```

Output lands in `firebase/build/dokka/html`.

## Testing

```bash
./gradlew :firebase:test
```

Unit tests run on Robolectric — no device or emulator required.

## License

Tealium Prism Firebase Dispatcher is available under a commercial license. See the
[LICENSE](./LICENSE) file for more info.
