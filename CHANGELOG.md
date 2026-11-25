# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0]

### Changed
- **Breaking:** requires Tealium Prism Core 0.8.0+.
- Command payloads are read with the Prism 0.8.0 extraction helpers. A parameter that can't be read now fails the command with `CommandException.invalidPayload`, whose cause is the matching `ExtractionException` (`Missing`, `TypeMismatch`, `InvalidValue` or `Empty`). An explicit `null` for a required parameter is reported as `Missing` instead of an invalid type. Name and value arrays of different lengths are still reported with `CommandException.arrayLengthMismatch`. The error messages, shown only in the `CommandDispatcher` log, changed accordingly and never include the parameter's value.
- `logevent` fails when `event_name` is blank. Firebase Analytics requires event names of 1 to 40 characters that start with a letter.
- `setconsent` trims the consent status before matching it, and reports a blank or `null` status as such. An empty `consent_settings` object is now reported as `Empty` instead of `noValidParameters`.
- `setuserproperty` reads the entries of the `property_name` and `property_value` arrays like single values, so numbers and booleans are converted to strings. They used to be skipped (names) or to clear the property (values).
- `setuserproperty` reports a `property_name` that is an object as `TypeMismatch` instead of `Empty`, and checks that the name and value arrays have the same length before checking that a name is usable.
- The release AAR is now minified and obfuscated with R8. The public API is unchanged and the `com.tealium.prism.firebase.internal` package is no longer readable in the published artifact.
- **Breaking:** minSdk raised to 24, as required by Firebase BoM 35 (Firebase Analytics 24.0.0).
- Updated Firebase BoM to 35.2.0 and Kotlin to 2.3.0. Consuming apps need Kotlin 2.2 or later.

## [0.2.0]

### Added
- The Firebase dispatcher now registers itself as a default module at app startup, so it is enabled purely from `SdkSettings` containing a `ModuleSettings` for the Firebase module type, without requiring `TealiumConfig.addModule`.

### Changed
- Requires Tealium Prism Core 0.7.0+.

## [0.1.0]

### Added
- Initial release of Tealium Prism Firebase Dispatcher for Android.
- Support for 8 Firebase Analytics commands:
  - `logevent` — log events with parameters and items (e-commerce).
  - `setuserid` — set or clear the Firebase user ID.
  - `setuserproperty` — set user properties (single or batch).
  - `resetdata` — reset all Firebase Analytics data.
  - `setdefaultparameters` — set default event parameters.
  - `setconsent` — configure consent settings (ad_storage, analytics_storage, ad_user_data, ad_personalization).
  - `setsessiontimeout` — update session timeout duration.
  - `setanalyticscollectionenabled` — enable or disable analytics collection.
- Type-safe mappings DSL with `FirebaseCommand`, `FirebaseDestination`, and `FirebaseMappings`.
- Builder-based configuration via `FirebaseSettingsBuilder`.
- Support for items in parallel-arrays and array-of-objects formats.
- Robolectric-based unit tests.
- Compose example app demonstrating all commands.

[0.3.0]: https://github.com/Tealium/tealium-prism-android-firebase-dispatcher/releases/tag/0.3.0
[0.2.0]: https://github.com/Tealium/tealium-prism-android-firebase-dispatcher/releases/tag/0.2.0
[0.1.0]: https://github.com/Tealium/tealium-prism-android-firebase-dispatcher/releases/tag/0.1.0
