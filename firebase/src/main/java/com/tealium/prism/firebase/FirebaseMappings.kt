package com.tealium.prism.firebase

import com.tealium.prism.core.api.command.CommandMappingsBuilder

/**
 * Concrete Firebase mappings builder combining [FirebaseCommand] and [FirebaseDestination].
 *
 * Used with `FirebaseSettingsBuilder.setMappings(…)` to configure Firebase-specific
 * data mappings with type-safe enums.
 *
 * ```kotlin
 * Modules.firebase {
 *     it.setMappings {
 *         mapCommand(FirebaseCommand.LOG_EVENT)
 *         mapFrom("tealium_event", FirebaseDestination.EventName)
 *         mapFrom("total", FirebaseDestination.EventParam(FirebaseAnalytics.Param.VALUE))
 *         mapFrom("product_ids", FirebaseDestination.ItemParam(FirebaseAnalytics.Param.ITEM_ID))
 *     }
 * }
 * ```
 */
class FirebaseMappings : CommandMappingsBuilder<FirebaseCommand, FirebaseDestination>()
