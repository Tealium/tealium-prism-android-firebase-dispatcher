package com.tealium.prism.firebase.internal

import com.tealium.prism.core.api.command.CommandDispatcher
import com.tealium.prism.core.api.data.DataObject
import com.tealium.prism.core.api.logger.Logger
import com.tealium.prism.core.api.logger.logIfDebugEnabled
import com.tealium.prism.core.api.misc.Scheduler
import com.tealium.prism.core.api.modules.Module
import com.tealium.prism.core.api.modules.ModuleFactory
import com.tealium.prism.core.api.modules.TealiumContext
import com.tealium.prism.firebase.Firebase
import com.tealium.prism.firebase.internal.commands.LogEventCommand
import com.tealium.prism.firebase.internal.commands.ResetDataCommand
import com.tealium.prism.firebase.internal.commands.SetAnalyticsCollectionEnabledCommand
import com.tealium.prism.firebase.internal.commands.SetConsentCommand
import com.tealium.prism.firebase.internal.commands.SetDefaultParametersCommand
import com.tealium.prism.firebase.internal.commands.SetSessionTimeoutCommand
import com.tealium.prism.firebase.internal.commands.SetUserIdCommand
import com.tealium.prism.firebase.internal.commands.SetUserPropertyCommand

/**
 * Firebase Analytics Dispatcher for the Tealium Prism SDK.
 *
 * Subclasses [CommandDispatcher] and registers one [com.tealium.prism.core.api.command.Command]
 * per Firebase operation exposed by the Confluence spec.
 */
internal class FirebaseDispatcher(
    private val firebaseInstance: FirebaseAnalyticsInterface,
    private var configuration: FirebaseDispatcherConfiguration,
    logger: Logger,
    scheduler: Scheduler,
) : CommandDispatcher(
    id = Firebase.ID,
    version = FirebaseConstants.VERSION,
    commands = listOf(
        SetSessionTimeoutCommand(firebaseInstance),
        SetAnalyticsCollectionEnabledCommand(firebaseInstance),
        LogEventCommand(firebaseInstance),
        SetUserPropertyCommand(firebaseInstance),
        SetDefaultParametersCommand(firebaseInstance),
        SetUserIdCommand(firebaseInstance),
        ResetDataCommand(firebaseInstance),
        SetConsentCommand(firebaseInstance),
    ),
    logger = logger,
    logCategory = Firebase.ID,
    scheduler = scheduler,
) {

    init {
        applySettings(configuration)
    }

    override fun updateConfiguration(configuration: DataObject): Module {
        val cfg = FirebaseDispatcherConfiguration.fromDataObject(configuration)
        this.configuration = cfg
        applySettings(cfg)
        return this
    }

    private fun applySettings(cfg: FirebaseDispatcherConfiguration) {
        logger.logIfDebugEnabled(logCategory) { "Applying configuration settings" }
        cfg.logLevel?.let {
            logger.logIfDebugEnabled(logCategory) {
                "log_level config key has no effect on Android"
            }
        }
        cfg.sessionTimeoutSeconds?.let { seconds ->
            val millis = (seconds * MILLISECONDS_PER_SECOND).toLong()
            firebaseInstance.setSessionTimeoutDuration(millis)
            logger.logIfDebugEnabled(logCategory) {
                "Session timeout set to $seconds seconds from configuration"
            }
        }
        cfg.analyticsCollectionEnabled?.let { enabled ->
            firebaseInstance.setAnalyticsCollectionEnabled(enabled)
            logger.logIfDebugEnabled(logCategory) {
                "Analytics collection enabled: $enabled from configuration"
            }
        }
    }

    class Factory(
        enforcedSettings: DataObject? = null,
    ) : ModuleFactory {

        private val enforcedList: List<DataObject> =
            enforcedSettings?.let { listOf(it) } ?: emptyList()

        override val moduleType: String = Firebase.ID

        override fun getEnforcedSettings(): List<DataObject> = enforcedList

        override fun create(
            moduleId: String,
            context: TealiumContext,
            configuration: DataObject,
        ): Module =
            FirebaseDispatcher(
                firebaseInstance = FirebaseInstance(context.context),
                configuration = FirebaseDispatcherConfiguration.fromDataObject(configuration),
                logger = context.logger,
                scheduler = context.schedulers.tealium,
            )
    }

    private companion object {
        const val MILLISECONDS_PER_SECOND = 1_000.0
    }
}
