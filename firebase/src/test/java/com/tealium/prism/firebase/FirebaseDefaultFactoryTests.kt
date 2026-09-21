package com.tealium.prism.firebase

import android.content.ComponentName
import android.content.pm.PackageManager
import com.tealium.prism.core.api.Modules
import com.tealium.prism.core.api.Tealium
import com.tealium.prism.core.api.TealiumConfig
import com.tealium.prism.core.api.misc.Environment
import com.tealium.prism.core.internal.misc.TealiumAutoInitContentProvider
import com.tealium.prism.firebase.internal.FirebaseDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Covers the Firebase dispatcher's `DEFAULT_FACTORY`, the manifest wiring that lets
 * `ComponentDiscoveryService` (core) discover it, and the resulting end-to-end behaviour: the
 * Firebase module enables purely from `SdkSettings`, without being added to `TealiumConfig`.
 *
 * Robolectric does not instantiate manifest-declared `ContentProvider`s at (simulated) app start
 * the way a real device does, so `TealiumAutoInitContentProvider` is triggered explicitly via
 * `Robolectric.setupContentProvider`, which runs its real `onCreate()` — the same production path
 * (`ComponentDiscoveryService.discoverModules` + `Modules.addDefaultModules`) that fires on-device.
 */
@RunWith(RobolectricTestRunner::class)
class FirebaseDefaultFactoryTests {

    private var tealium: Tealium? = null

    @After
    fun tearDown() {
        tealium?.shutdown()
        tealium = null
    }

    @Test
    fun defaultFactory_HasCorrectModuleType() {
        assertEquals(Modules.Types.FIREBASE, DEFAULT_FACTORY.moduleType)
    }

    @Test
    fun defaultFactory_HasNoEnforcedSettings() {
        assertTrue(DEFAULT_FACTORY.getEnforcedSettings().isEmpty())
    }

    @Test
    fun componentDiscoveryServiceManifest_DeclaresFirebaseDefaultFactory() {
        // Confirms the manifest meta-data added to firebase/src/main/AndroidManifest.xml survives
        // manifest merging, with the key format required by ComponentDiscoveryService.
        val app = RuntimeEnvironment.getApplication()
        val serviceInfo = app.packageManager.getServiceInfo(
            ComponentName(app, "com.tealium.prism.core.internal.misc.ComponentDiscoveryService"),
            PackageManager.GET_META_DATA
        )

        val expectedKey =
            "com.tealium.prism.core.api.modules:com.tealium.prism.firebase.FirebaseKt#DEFAULT_FACTORY"
        assertTrue(serviceInfo.metaData?.containsKey(expectedKey) == true)
    }

    @Test
    fun tealium_EnablesFirebaseModule_FromSdkSettingsAlone_WithoutAddingItToTealiumConfig() {
        // Real app startup: core's auto-init content provider reads the manifest meta-data and
        // registers the Firebase factory as a default module, before any Tealium instance exists.
        Robolectric.setupContentProvider(TealiumAutoInitContentProvider::class.java)

        // No `addModule(Firebase...)` call here: the factory must come from the default module
        // registry populated above, matched against the `SdkSettings` from the local settings file.
        val config = TealiumConfig.Builder(
            RuntimeEnvironment.getApplication(),
            "test-account-enabled",
            "test-profile",
            Environment.DEV,
            emptyList()
        ).setSettingsFile("firebase_default_module_settings.json").build()

        val readyLatch = CountDownLatch(1)
        Tealium.create(config) { result ->
            tealium = result.getOrNull()
            readyLatch.countDown()
        }
        assertTrue("Tealium instance did not become ready in time", readyLatch.await(5, TimeUnit.SECONDS))
        assertNotNull(tealium)

        // The module list updates asynchronously as local settings are merged in after "ready",
        // so observe for it rather than taking a single synchronous snapshot right away.
        val moduleLatch = CountDownLatch(1)
        val subscription = tealium!!.createModuleProxy(FirebaseDispatcher::class.java)
            .observeModule()
            .subscribe { module -> if (module != null) moduleLatch.countDown() }
        val moduleFound = moduleLatch.await(5, TimeUnit.SECONDS)
        subscription.dispose()
        assertTrue("Firebase module should be enabled purely from SdkSettings", moduleFound)
    }

    @Test
    fun tealium_DoesNotEnableFirebaseModule_WithoutSdkSettings() {
        // Same startup simulation as the positive test, so the only difference is the absence of
        // a settings file below — proving the module needs the SdkSettings entry, not just the
        // factory being registered as a default module.
        Robolectric.setupContentProvider(TealiumAutoInitContentProvider::class.java)

        val config = TealiumConfig.Builder(
            RuntimeEnvironment.getApplication(),
            "test-account-disabled",
            "test-profile",
            Environment.DEV,
            emptyList()
        ).build()

        val readyLatch = CountDownLatch(1)
        Tealium.create(config) { result ->
            tealium = result.getOrNull()
            readyLatch.countDown()
        }
        assertTrue("Tealium instance did not become ready in time", readyLatch.await(5, TimeUnit.SECONDS))
        assertNotNull(tealium)

        // No settings ever enable Firebase here, so a single post-ready snapshot is sufficient:
        // the module will never appear, there is no async arrival to race against.
        var module: FirebaseDispatcher? = null
        val moduleLatch = CountDownLatch(1)
        tealium!!.createModuleProxy(FirebaseDispatcher::class.java).getModule { result ->
            module = result
            moduleLatch.countDown()
        }
        assertTrue("getModule callback did not complete in time", moduleLatch.await(5, TimeUnit.SECONDS))
        assertNull("Firebase module should not be enabled without matching SdkSettings", module)
    }
}
