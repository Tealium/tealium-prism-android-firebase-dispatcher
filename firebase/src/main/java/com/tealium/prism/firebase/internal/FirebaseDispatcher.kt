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
import com.tealium.prism.firebase.internal.commands.logEventCommand
import com.tealium.prism.firebase.internal.commands.resetDataCommand
import com.tealium.prism.firebase.internal.commands.setAnalyticsCollectionEnabledCommand
import com.tealium.prism.firebase.internal.commands.setConsentCommand
import com.tealium.prism.firebase.internal.commands.setDefaultParametersCommand
import com.tealium.prism.firebase.internal.commands.setSessionTimeoutCommand
import com.tealium.prism.firebase.internal.commands.setUserIdCommand
import com.tealium.prism.firebase.internal.commands.setUserPropertyCommand

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
        setSessionTimeoutCommand(firebaseInstance),
        setAnalyticsCollectionEnabledCommand(firebaseInstance),
        logEventCommand(firebaseInstance),
        setUserPropertyCommand(firebaseInstance),
        setDefaultParametersCommand(firebaseInstance),
        setUserIdCommand(firebaseInstance),
        resetDataCommand(firebaseInstance),
        setConsentCommand(firebaseInstance),
    ),
    logger = logger,
    logCategory = Firebase.ID,
    scheduler = scheduler,
) {

    init {
        applySettings(configuration)
    }

    override fun updateConfiguration(configuration: DataObject): Module {
        val config = FirebaseDispatcherConfiguration.fromDataObject(configuration)
        this.configuration = config
        applySettings(config)
        return this
    }

    private fun applySettings(config: FirebaseDispatcherConfiguration) {
        logger.logIfDebugEnabled(logCategory) { "Applying configuration settings" }
        config.logLevel?.let {
            logger.logIfDebugEnabled(logCategory) {
                "log_level config key has no effect on Android"
            }
        }
        config.sessionTimeoutSeconds?.let { seconds ->
            val millis = (seconds * FirebaseConstants.MILLISECONDS_PER_SECOND).toLong()
            firebaseInstance.setSessionTimeoutDuration(millis)
            logger.logIfDebugEnabled(logCategory) {
                "Session timeout set to $seconds seconds from configuration"
            }
        }
        config.analyticsCollectionEnabled?.let { enabled ->
            firebaseInstance.setAnalyticsCollectionEnabled(enabled)
            logger.logIfDebugEnabled(logCategory) {
                "Analytics collection enabled: $enabled from configuration"
            }
        }
    }

    class Factory(
        enforcedSettings: DataObject? = null
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

}
