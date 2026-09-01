package com.tealium.prism.firebase

import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.modules.Module
import com.tealium.prism.core.api.modules.ModuleFactory
import com.tealium.prism.firebase.internal.FirebaseDispatcher

/**
 * Firebase Analytics Command Dispatcher module.
 *
 * Routes tracking events to the Firebase Analytics Android SDK via the Tealium Prism
 * Command Dispatcher pattern.
 *
 * Firebase must be initialized before the first command executes — typically via
 * automatic initialization by the `firebase-analytics` content provider, or explicitly
 * via `FirebaseApp.initializeApp(context)` in `Application.onCreate()`. The dispatcher
 * obtains its `FirebaseAnalytics` instance through `FirebaseAnalytics.getInstance(context)`.
 */
object Firebase {

    /**
     * The [Module.id] of the Firebase dispatcher module.
     */
    const val ID: String = "FirebaseDispatcher"

    /**
     * Returns a configured [ModuleFactory] for enabling the Firebase dispatcher.
     *
     * @param enforcedSettings
     *  Firebase dispatcher settings that should override any from any other settings source.
     *  Pass `null` to initialize this module only when some Local or Remote settings are provided.
     *  Omitting this parameter will initialize the module with its default settings.
     */
    @JvmStatic
    @JvmOverloads
    fun configure(
        enforcedSettings: ((FirebaseSettingsBuilder) -> FirebaseSettingsBuilder)? = { it }
    ): ModuleFactory {
        val builder = enforcedSettings?.invoke(FirebaseSettingsBuilder())
        return FirebaseDispatcher.Factory(builder?.build())
    }
}

/**
 * Returns the default [ModuleFactory] implementation that will not create any instances
 * unless there are settings provided from Local or Remote sources.
 */
@JvmField
val DEFAULT_FACTORY: ModuleFactory = Firebase.configure(null)

/**
 * Returns a configured [ModuleFactory] for enabling the Firebase dispatcher.
 *
 * @param enforcedSettings
 *  Firebase dispatcher settings that should override any from any other settings source.
 *  Pass `null` to initialize this module only when some Local or Remote settings are provided.
 *  Omitting this parameter will initialize the module with its default settings.
 */
@JvmOverloads
fun Modules.firebase(
    enforcedSettings: ((FirebaseSettingsBuilder) -> FirebaseSettingsBuilder)? = { it }
): ModuleFactory = Firebase.configure(enforcedSettings)

/**
 * The [Module.id] of the Firebase dispatcher module.
 */
val Modules.Types.FIREBASE: String
    get() = Firebase.ID
