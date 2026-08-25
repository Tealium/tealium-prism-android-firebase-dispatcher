# Module Tealium Prism Firebase Dispatcher

The Firebase module is a `Dispatcher` module that routes Tealium Prism tracking events to the Firebase
Analytics Android SDK — events, user properties, consent settings, and more. Firebase Analytics only
supports a single shared instance, so only one Firebase dispatcher can be active at a time in your app.

Firebase must be initialized before the first command executes. This normally happens automatically via
the `firebase-analytics` content provider; if another Firebase product needs a specific initialization
order, call `FirebaseApp.initializeApp(context)` in `Application.onCreate()` before `Tealium.create(...)`.
The dispatcher obtains its instance through `FirebaseAnalytics.getInstance(context)`.

## Installation/Configuration

The Firebase module can be configured using three different approaches.

### Local and Remote Settings

Configure the module using a local JSON settings file (via `setSettingsFile`) and/or remote settings
(via `setSettingsUrl`):

```kotlin
val config = TealiumConfig.Builder(
    application = application,
    accountName = "tealiummobile",
    profileName = "your-profile",
    environment = "dev",
    modules = emptyList(),
)
    .setSettingsFile("tealium-settings.json")
    .setSettingsUrl("https://tags.tiqcdn.com/dle/tealiummobile/lib/example_settings.json")
    .build()
```

**Default initialization** — the module is initialized only if configured in the settings file:

```json
{
    "modules": {
        "FirebaseDispatcher": {
            "module_type": "FirebaseDispatcher"
        }
    }
}
```

**Custom configuration** — the module with specific settings:

```json
{
    "modules": {
        "FirebaseDispatcher": {
            "module_type": "FirebaseDispatcher",
            "enabled": true,
            "order": 1,
            "configuration": {
                "session_timeout_seconds": 1800,
                "analytics_collection_enabled": true
            }
        }
    }
}
```

If a setting is omitted, Firebase applies its own default value. When both local and remote settings are
provided they are deep merged, with remote settings taking priority.

### Programmatic Configuration

Configure the module programmatically by adding it to the `modules` list in `TealiumConfig.Builder`.

**Default initialization** — the module is initialized with its default settings:

```kotlin
val config = TealiumConfig.Builder(
    application = application,
    accountName = "tealiummobile",
    profileName = "your-profile",
    environment = "dev",
    modules = listOf(
        Modules.firebase(),
        // other modules...
    ),
).build()
```

```java
// Java
TealiumConfig config = new TealiumConfig.Builder(
        application, "tealiummobile", "your-profile", "dev",
        Arrays.asList(Firebase.configure())
).build();
```

**Custom configuration** — the module with enforced settings:

```kotlin
val config = TealiumConfig.Builder(
    application = application,
    accountName = "tealiummobile",
    profileName = "your-profile",
    environment = "dev",
    modules = listOf(
        Modules.firebase { builder ->
            builder
                .setEnabled(true)
                .setOrder(1)
                .setSessionTimeout(30.minutes)
                .setAnalyticsEnabled(true)
        },
        // other modules...
    ),
).build()
```

Passing `null` instead of a lambda — `Modules.firebase(null)`, or the equivalent
[DEFAULT_FACTORY] — initializes the module *only* when local or remote settings are provided.

> **Important:** Programmatic settings are deep merged onto local and remote settings and always take
> precedence. Only provide programmatic settings for configuration values that you never want to be
> changed remotely, as they will override any remote updates.

## Configuration Options

The Firebase module supports the following configuration options:

| Setting | Key | Description | Default Value |
|---------|-----|-------------|---------------|
| Session timeout | `session_timeout_seconds` | Session timeout in seconds. Expressed in seconds to match the cross-platform payload schema; converted to milliseconds before being applied via `setSessionTimeoutDuration`. | *Firebase default* |
| Analytics collection enabled | `analytics_collection_enabled` | Enables or disables Firebase Analytics data collection. When disabled, collection stops but previously collected data is retained. | *Firebase default* |

Omitted settings are left untouched on the SDK — only explicitly provided values are applied.

There is no `log_level` setting on Android; the Firebase Android SDK has no equivalent of the iOS
`FirebaseLoggerLevel` API. Enable verbose Firebase logging with `adb shell setprop log.tag.FA VERBOSE`
instead.

## Settings Builders Reference

The Firebase module uses [FirebaseSettingsBuilder] for configuration. This extends
`DispatcherSettingsBuilder<FirebaseMappings, FirebaseSettingsBuilder>`, which offers common settings
such as:

- `ModuleSettingsBuilder.setEnabled`
- `ModuleSettingsBuilder.setOrder`
- `RuleModuleSettingsBuilder.setRules`
- `DispatcherSettingsBuilder.setMappings`

### Firebase-specific methods

- [FirebaseSettingsBuilder.setSessionTimeout] — set the session timeout duration (e.g. `30.minutes`)
- [FirebaseSettingsBuilder.setAnalyticsEnabled] — enable or disable analytics collection

`30.minutes` comes from `com.tealium.prism.core.api.misc.TimeFrameUtils.minutes` — import it explicitly
so it is not confused with `kotlin.time.Duration.Companion.minutes`.

## Commands

| Command | Firebase API |
|---------|--------------|
| `logevent` | `logEvent(name, Bundle)` |
| `setuserid` | `setUserId(id)` |
| `setuserproperty` | `setUserProperty(name, value)` |
| `resetdata` | `resetAnalyticsData()` |
| `setdefaultparameters` | `setDefaultEventParameters(Bundle)` |
| `setconsent` | `setConsent(Map<ConsentType, ConsentStatus>)` |
| `setsessiontimeout` | `setSessionTimeoutDuration(ms)` |
| `setanalyticscollectionenabled` | `setAnalyticsCollectionEnabled(bool)` |

`initiateconversionmeasurement` is iOS only — the Firebase Android SDK does not expose the on-device
conversion measurement API.

Each command is driven by the [FirebaseMappings] system, using the type-safe [FirebaseCommand] and
[FirebaseDestination] enums:

```kotlin
Modules.firebase { builder ->
    builder.setMappings {
        mapCommand(FirebaseCommand.LOG_EVENT).forAllEvents()
        mapFrom("tealium_event", FirebaseDestination.EventName)
        mapFrom("total", FirebaseDestination.EventParam(Param.VALUE))
        mapFrom("product_ids", FirebaseDestination.ItemParam(Param.ITEM_ID))
    }
}
```

Item arrays under `parameters.items` accept two equivalent shapes — parallel arrays (the Tealium
convention, one array of values per property) and an array of objects (the Firebase-ready shape). Both
produce an `ArrayList<Bundle>` under `FirebaseAnalytics.Param.ITEMS`.

Consent settings accept the string types `ad_storage`, `analytics_storage`, `ad_user_data` and
`ad_personalization`, with the statuses `granted` and `denied`. Unknown types or statuses fail the
command, because the Android SDK enums are closed and an unrecognized value cannot be forwarded.

See the
[README](https://github.com/Tealium/tealium-prism-android-firebase-dispatcher#commands)
for the full JSON and programmatic mapping reference for every command, including item formats, consent
types, and the rules for clearing default parameters.
