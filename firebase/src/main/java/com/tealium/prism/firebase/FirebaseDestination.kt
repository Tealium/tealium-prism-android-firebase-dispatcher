package com.tealium.prism.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.tealium.prism.core.api.data.JsonObjectPath
import com.tealium.prism.core.api.data.JsonObjectPathConvertible
import com.tealium.prism.core.api.data.JsonPath
import com.tealium.prism.firebase.internal.ConsentConverter

/**
 * Type-safe Firebase Analytics mapping destinations.
 *
 * Each case maps to a specific key or path in the Firebase command payload.
 * Use with `CommandMappingsBuilder.mapFrom(…)`, `mapConstant(…)`, and `keep(…)`.
 */
sealed class FirebaseDestination : JsonObjectPathConvertible {

    // MARK: LogEvent

    /** The event name parameter (`"event_name"`). */
    object EventName : FirebaseDestination()

    /** The event parameters dictionary (`"parameters"`). */
    object EventParams : FirebaseDestination()

    /**
     * A specific event parameter nested under `parameters.[name]`.
     *
     * Pass a Firebase Analytics parameter constant (e.g. [FirebaseAnalytics.Param.CURRENCY])
     * or any custom string for non-predefined parameters.
     */
    data class EventParam(val param: String) : FirebaseDestination()

    /**
     * A specific item parameter nested under `parameters.items.[name]`.
     *
     * Pass a Firebase Analytics item parameter constant (e.g. [FirebaseAnalytics.Param.ITEM_ID])
     * or any custom string for non-predefined parameters.
     */
    data class ItemParam(val param: String) : FirebaseDestination()

    // MARK: SetUserId

    /** The user ID parameter (`"user_id"`). */
    object UserId : FirebaseDestination()

    // MARK: SetUserProperty

    /** The user property name(s) parameter (`"property_name"`). */
    object UserPropertyName : FirebaseDestination()

    /** The user property value(s) parameter (`"property_value"`). */
    object UserPropertyValue : FirebaseDestination()

    // MARK: SetDefaultParameters

    /** The default parameters dictionary (`"parameters"`). */
    object DefaultParams : FirebaseDestination()

    /** A specific default parameter nested under `parameters.[name]`. */
    data class DefaultParam(val name: String) : FirebaseDestination()

    // MARK: SetConsent

    /** The consent settings dictionary (`"consent_settings"`). */
    object ConsentSettings : FirebaseDestination()

    /** A specific consent setting nested under `consent_settings.[type]`. */
    data class ConsentSetting(val type: FirebaseAnalytics.ConsentType) : FirebaseDestination()

    // MARK: SetSessionTimeout

    /** The session timeout parameter (`"session_timeout_seconds"`). */
    object SessionTimeout : FirebaseDestination()

    // MARK: SetAnalyticsCollectionEnabled

    /** The analytics enabled parameter (`"analytics_collection_enabled"`). */
    object AnalyticsEnabled : FirebaseDestination()

    override fun asJsonObjectPath(): JsonObjectPath = when (this) {
        EventName -> JsonPath["event_name"]
        EventParams -> JsonPath["parameters"]
        is EventParam -> JsonPath["parameters"].key(param)
        is ItemParam -> JsonPath["parameters"].key(FirebaseAnalytics.Param.ITEMS).key(param)
        UserId -> JsonPath["user_id"]
        UserPropertyName -> JsonPath["property_name"]
        UserPropertyValue -> JsonPath["property_value"]
        DefaultParams -> JsonPath["parameters"]
        is DefaultParam -> JsonPath["parameters"].key(name)
        ConsentSettings -> JsonPath["consent_settings"]
        is ConsentSetting -> JsonPath["consent_settings"].key(ConsentConverter.rawValue(type))
        SessionTimeout -> JsonPath["session_timeout_seconds"]
        AnalyticsEnabled -> JsonPath["analytics_collection_enabled"]
    }
}
